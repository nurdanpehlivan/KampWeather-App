package com.nurdan.hava.testing

import com.nurdan.hava.data.CityRepository
import com.nurdan.hava.data.LocationRepository
import com.nurdan.hava.data.SavedCitiesRepository
import com.nurdan.hava.data.WeatherRepository
import com.nurdan.hava.model.City
import com.nurdan.hava.model.CurrentWeather
import com.nurdan.hava.model.Forecast
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Testlerde gerçek repository yerine kullanılan sahteler.
 *
 * Repository'yi arayüz olarak tanımlamamızın sebebi tam olarak budur: ViewModel
 * testleri internete çıkmadan, istenen her sonucu (başarı / boş / hata)
 * kolayca üretebilir.
 */

/**
 * @param result Her çağrıda dönecek liste.
 * @param error Doluysa [search] bu hatayı fırlatır.
 */
class FakeCityRepository(
    var result: List<City> = emptyList(),
    var error: Throwable? = null
) : CityRepository {

    /** Son aranan metin; ViewModel'in doğru sorguyu ilettiğini doğrulamak için. */
    var lastQuery: String? = null
        private set

    /** Kaç kez arama yapıldığı; "Tekrar dene" davranışını doğrulamak için. */
    var callCount: Int = 0
        private set

    override suspend fun search(query: String): List<City> {
        lastQuery = query
        callCount++
        error?.let { throw it }
        return result
    }
}

/** @param error Doluysa [getForecast] bu hatayı fırlatır. */
class FakeWeatherRepository(
    var result: Forecast = sampleForecast(),
    var error: Throwable? = null
) : WeatherRepository {

    var callCount: Int = 0
        private set

    override suspend fun getForecast(latitude: Double, longitude: Double): Forecast {
        callCount++
        error?.let { throw it }
        return result
    }
}

/** Testlerde tekrar tekrar yazmamak için hazır bir şehir. */
fun sampleCity(id: Long = 323786L, name: String = "Ankara") = City(
    id = id,
    name = name,
    latitude = 39.91987,
    longitude = 32.85427,
    countryCode = "TR",
    country = "Türkiye",
    admin1 = "Ankara"
)

/** Testlerde tekrar tekrar yazmamak için hazır bir tahmin. */
fun sampleForecast() = Forecast(
    current = CurrentWeather(
        temperatureC = 21.0,
        weatherCode = 0,
        observedAt = LocalDateTime.parse("2026-09-24T11:30")
    ),
    hourly = emptyList(),
    daily = emptyList()
)

/** Bellekte tutulan favoriler; DataStore yerine. */
class FakeSavedCitiesRepository : SavedCitiesRepository {
    private val state = MutableStateFlow<List<City>>(emptyList())
    override val favorites: Flow<List<City>> = state

    var lastViewed: City? = null
        private set

    override suspend fun toggleFavorite(city: City) {
        state.value = if (state.value.any { it.id == city.id }) state.value.filterNot { it.id == city.id } else state.value + city
    }

    override suspend fun setLastViewed(city: City) {
        lastViewed = city
    }

    override suspend fun widgetCity(): City? = state.value.firstOrNull() ?: lastViewed
}

/** @param result Konum isteğinde dönecek şehir; `null` ise konum bulunamamış sayılır. */
class FakeLocationRepository(var result: City? = null) : LocationRepository {
    override suspend fun currentCity(): City? = result
}
