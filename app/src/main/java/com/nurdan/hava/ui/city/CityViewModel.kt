package com.nurdan.hava.ui.city

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonSyntaxException
import com.nurdan.hava.AppGraph
import com.nurdan.hava.data.CityRepository
import com.nurdan.hava.data.LocationRepository
import com.nurdan.hava.data.OpenMeteoCityRepository
import com.nurdan.hava.data.SavedCitiesRepository
import com.nurdan.hava.model.City
import com.nurdan.hava.ui.LoadState
import java.io.IOException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * Şehir ekranının beyni: arama metni, sonuç durumu ve seçili şehir burada yaşar.
 *
 * ViewModel ekran döndürülünce de hayatta kalır; arama metni, sonuçlar ve vurgulanan
 * şehir kaybolmaz.
 *
 * Yazdıkça arama: kullanıcı yazmayı [TYPING_DEBOUNCE_MS] kadar bıraktığında arama kendiliğinden
 * başlar. Her harfte istek atılmaz; "İst" → "İsta" → "İstan" yazarken tek istek gider.
 */
@OptIn(FlowPreview::class)
class CityViewModel(
    private val repository: CityRepository = OpenMeteoCityRepository(),
    private val savedCities: SavedCitiesRepository = AppGraph.savedCities,
    private val locationRepository: LocationRepository = AppGraph.location
) : ViewModel() {

    private val _query = MutableStateFlow(INITIAL_QUERY)
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow<LoadState<List<City>>>(LoadState.Loading)
    val uiState: StateFlow<LoadState<List<City>>> = _uiState.asStateFlow()

    /** En son dokunulan şehir; listede vurgulanır. */
    private val _selectedCityId = MutableStateFlow<Long?>(null)
    val selectedCityId: StateFlow<Long?> = _selectedCityId.asStateFlow()

    /** Favori şehirler; uygulama kapansa da DataStore'da saklanır. */
    val favorites: StateFlow<List<City>> = savedCities.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _location = MutableStateFlow<LocationState>(LocationState.Idle)

    /** "Konumum" düğmesinin durumu. */
    val location: StateFlow<LocationState> = _location.asStateFlow()

    private var searchJob: Job? = null

    init {
        search()
        // İlk değer (açılış araması) zaten yapıldı; yalnızca kullanıcının yazdıklarını dinle.
        _query
            .drop(1)
            .debounce(TYPING_DEBOUNCE_MS)
            .filter { it.trim().length >= MIN_QUERY_LENGTH }
            .onEach { search() }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    /** Karta dokunulunca: şehri vurgula ve Logcat'e yaz (filtre: `CityCard`). */
    fun onCitySelected(city: City) {
        Log.d(CLICK_TAG, "Tıklandı: ${city.name}")
        _selectedCityId.value = city.id
        viewModelScope.launch { savedCities.setLastViewed(city) }
    }

    fun onToggleFavorite(city: City) {
        viewModelScope.launch { savedCities.toggleFavorite(city) }
    }

    /** Konum izni verildikten sonra çağrılır. */
    fun onLocate() {
        if (_location.value == LocationState.Searching) return
        viewModelScope.launch {
            _location.value = LocationState.Searching
            _location.value = locationRepository.currentCity()?.let { LocationState.Found(it) } ?: LocationState.Failed
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
        searchJob = viewModelScope.launch {
            _uiState.value = LoadState.Loading
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

    internal companion object {
        private const val TAG = "CityViewModel"
        private const val CLICK_TAG = "CityCard"

        /** Uygulama ilk açıldığında aranan şehir. */
        const val INITIAL_QUERY = "İstanbul"

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
