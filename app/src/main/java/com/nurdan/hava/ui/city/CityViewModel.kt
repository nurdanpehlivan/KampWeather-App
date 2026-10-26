package com.nurdan.hava.ui.city

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonSyntaxException
import com.nurdan.hava.AppGraph
import com.nurdan.hava.data.CityRepository
import com.nurdan.hava.data.LocationRepository
import com.nurdan.hava.data.OpenMeteoCityRepository
import com.nurdan.hava.data.OpenMeteoWeatherRepository
import com.nurdan.hava.data.SavedCitiesRepository
import com.nurdan.hava.data.WeatherRepository
import com.nurdan.hava.model.City
import com.nurdan.hava.model.Forecast
import com.nurdan.hava.ui.LoadState
import java.io.IOException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * Ana ekranda bir şehir kartı: şehir, konumdan mı geldiği ve anlık havası.
 */
data class CityWeather(val city: City, val isMyLocation: Boolean, val weather: LoadState<Forecast>)

/**
 * Ana ekranın beyni.
 *
 * İki modu vardır:
 * - Arama kutusu boşken **Şehirlerim**: konum şehri + favoriler, her biri anlık havasıyla.
 * - Bir şey yazılınca **arama**: yazmayı [TYPING_DEBOUNCE_MS] kadar bırakınca arama
 *   kendiliğinden başlar; her harfte istek atılmaz.
 *
 * ViewModel ekran döndürülünce de yaşar; arama, sonuçlar ve seçili şehir kaybolmaz.
 */
@OptIn(FlowPreview::class)
class CityViewModel(
    private val repository: CityRepository = OpenMeteoCityRepository(),
    private val savedCities: SavedCitiesRepository = AppGraph.savedCities,
    private val locationRepository: LocationRepository = AppGraph.location,
    private val weatherRepository: WeatherRepository = OpenMeteoWeatherRepository()
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    /** Arama sonucunun dört durumu. Henüz arama yapılmadıysa Empty. */
    private val _uiState = MutableStateFlow<LoadState<List<City>>>(LoadState.Empty)
    val uiState: StateFlow<LoadState<List<City>>> = _uiState.asStateFlow()

    /** En son dokunulan şehir; arama sonuçlarında vurgulanır. */
    private val _selectedCityId = MutableStateFlow<Long?>(null)
    val selectedCityId: StateFlow<Long?> = _selectedCityId.asStateFlow()

    /** Favori şehirler; uygulama kapansa da DataStore'da saklanır. */
    val favorites: StateFlow<List<City>> = savedCities.favorites
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Şehir kimliği → o şehrin anlık havası. Her şehir için bir kez yüklenir. */
    private val weatherById = MutableStateFlow<Map<Long, LoadState<Forecast>>>(emptyMap())

    /** Ana ekran kartları: önce konum şehri, ardından favoriler (konumla aynı olan tekrar edilmez). */
    val myCities: StateFlow<List<CityWeather>> = combine(
        savedCities.locationCity,
        savedCities.favorites,
        weatherById
    ) { location, favorites, weather ->
        val cards = listOfNotNull(location?.let { it to true }) +
            favorites.filterNot { it.id == location?.id }.map { it to false }
        cards.map { (city, isMine) -> CityWeather(city, isMine, weather[city.id] ?: LoadState.Loading) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _location = MutableStateFlow<LocationState>(LocationState.Idle)

    /** "Konumum" düğmesinin durumu. */
    val location: StateFlow<LocationState> = _location.asStateFlow()

    private var searchJob: Job? = null

    init {
        _query
            .drop(1)
            .debounce(TYPING_DEBOUNCE_MS)
            .filter { it.trim().length >= MIN_QUERY_LENGTH }
            .onEach { search() }
            .launchIn(viewModelScope)
        // Listeye yeni bir şehir girince havasını getir.
        myCities
            .onEach { cards -> cards.filter { it.city.id !in weatherById.value }.forEach { loadWeather(it.city) } }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    /** Karta dokunulunca: şehri vurgula, son bakılan olarak kaydet ve Logcat'e yaz (filtre: `CityCard`). */
    fun onCitySelected(city: City) {
        Log.d(CLICK_TAG, "Tıklandı: ${city.name}")
        _selectedCityId.value = city.id
        viewModelScope.launch { savedCities.setLastViewed(city) }
    }

    fun onToggleFavorite(city: City) {
        viewModelScope.launch { savedCities.toggleFavorite(city) }
    }

    /** Aşağı çekip yenileme: bütün kartların havası yeniden alınır. */
    fun onRefreshMyCities() {
        myCities.value.forEach { loadWeather(it.city) }
    }

    /** Konum izni verildikten sonra çağrılır. Bulunan şehir ana ekranın ilk kartı olur. */
    fun onLocate() {
        if (_location.value == LocationState.Searching) return
        viewModelScope.launch {
            _location.value = LocationState.Searching
            val city = locationRepository.currentCity()
            if (city != null) savedCities.setLocationCity(city)
            _location.value = city?.let { LocationState.Found(it) } ?: LocationState.Failed
        }
    }

    /** Ekran sonucu işledikten (gezinme, uyarı) sonra durumu sıfırlar. */
    fun onLocationHandled() {
        _location.value = LocationState.Idle
    }

    /**
     * Aramayı çalıştırır ve sonucu dört durumdan birine çevirir. Klavyedeki "Ara"
     * tuşu ve hata ekranındaki "Tekrar dene" de buraya bağlıdır.
     *
     * Önceki arama hâlâ sürüyorsa iptal edilir; eski bir yanıt yeni sonucu ezemez.
     */
    fun search() {
        val text = _query.value.trim()
        if (text.isEmpty()) return
        searchJob?.cancel()
        // Loading hemen gösterilir; iskelet kartlar istek beklenmeden ekrana gelir.
        _uiState.value = LoadState.Loading
        searchJob = viewModelScope.launch {
            _uiState.value = try {
                val cities = repository.search(text)
                if (cities.isEmpty()) LoadState.Empty else LoadState.Success(cities)
            } catch (e: IOException) {
                Log.e(TAG, "Arama başarısız: bağlantı hatası", e)
                LoadState.Error("İnternet bağlantını kontrol et.")
            } catch (e: HttpException) {
                Log.e(TAG, "Arama başarısız: HTTP ${e.code()}", e)
                LoadState.Error("Sunucu şu an cevap vermiyor.")
            } catch (e: JsonSyntaxException) {
                Log.e(TAG, "Arama başarısız: yanıt çözümlenemedi", e)
                LoadState.Error("Bir şeyler ters gitti. Tekrar dene.")
            }
        }
    }

    private fun loadWeather(city: City) {
        weatherById.update { it + (city.id to LoadState.Loading) }
        viewModelScope.launch {
            val state = try {
                LoadState.Success(weatherRepository.getForecast(city.latitude, city.longitude))
            } catch (e: IOException) {
                Log.e(TAG, "Kart havası alınamadı: ${city.name}", e)
                LoadState.Error("Bağlantı yok")
            } catch (e: HttpException) {
                Log.e(TAG, "Kart havası alınamadı: ${city.name}", e)
                LoadState.Error("Sunucu yanıt vermiyor")
            } catch (e: JsonSyntaxException) {
                Log.e(TAG, "Kart havası alınamadı: ${city.name}", e)
                LoadState.Error("Veri okunamadı")
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Kart havası alınamadı: ${city.name}", e)
                LoadState.Error("Veri eksik")
            }
            weatherById.update { it + (city.id to state) }
        }
    }

    internal companion object {
        private const val TAG = "CityViewModel"
        private const val CLICK_TAG = "CityCard"

        /** Yazmayı bıraktıktan sonra aramanın başlaması için beklenen süre. */
        const val TYPING_DEBOUNCE_MS = 450L

        /** Bundan kısa metinlerle otomatik arama yapılmaz. */
        const val MIN_QUERY_LENGTH = 2
    }
}

/** "Konumum" düğmesinin dört hâli. */
sealed interface LocationState {
    data object Idle : LocationState
    data object Searching : LocationState
    data class Found(val city: City) : LocationState
    data object Failed : LocationState
}
