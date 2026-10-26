package com.nurdan.hava.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.os.CancellationSignal
import androidx.core.util.Consumer
import com.nurdan.hava.model.City
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/** Telefonun bulunduğu yeri bir [City]'ye çevirir. */
interface LocationRepository {
    /** Konum alınamazsa (kapalı, zaman aşımı) `null`. İzin çağıran tarafından istenmiş olmalıdır. */
    suspend fun currentCity(): City?
}

/**
 * Google Play Services gerektirmeyen uygulama: Android'in kendi [LocationManager]'ı.
 * Şehir adı, cihazdaki [Geocoder] ile koordinattan bulunur.
 */
class AndroidLocationRepository(context: Context) : LocationRepository {

    private val appContext = context.applicationContext
    private val manager = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    override suspend fun currentCity(): City? {
        val location = withTimeoutOrNull(TIMEOUT_MS) { freshLocation() } ?: lastKnown() ?: return null
        return withContext(Dispatchers.IO) { toCity(location) }
    }

    @SuppressLint("MissingPermission") // İzin, ekran tarafında istenip doğrulanıyor.
    private suspend fun freshLocation(): Location? {
        val provider = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
            .firstOrNull { manager.isProviderEnabled(it) } ?: return null
        return suspendCancellableCoroutine { continuation ->
            LocationManagerCompat.getCurrentLocation(
                manager,
                provider,
                null as CancellationSignal?,
                ContextCompat.getMainExecutor(appContext),
                Consumer<Location?> { location -> if (continuation.isActive) continuation.resume(location) }
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun lastKnown(): Location? = manager.getProviders(true)
        .mapNotNull { manager.getLastKnownLocation(it) }
        .maxByOrNull { it.time }

    @Suppress("DEPRECATION") // Senkron getFromLocation, IO thread'inde çağrıldığı için uygun.
    private fun toCity(location: Location): City {
        val address = try {
            if (Geocoder.isPresent()) {
                Geocoder(appContext, Locale.forLanguageTag("tr")).getFromLocation(location.latitude, location.longitude, 1)?.firstOrNull()
            } else {
                null
            }
        } catch (e: IOException) {
            null
        }
        return City(
            id = MY_LOCATION_ID,
            name = address?.subAdminArea ?: address?.locality ?: address?.adminArea ?: "Konumum",
            latitude = location.latitude,
            longitude = location.longitude,
            countryCode = address?.countryCode,
            country = address?.countryName,
            admin1 = address?.adminArea
        )
    }

    companion object {
        /** Konumdan gelen şehrin kimliği; aramadan gelen kimliklerle çakışmaz. */
        const val MY_LOCATION_ID = -1L
        private const val TIMEOUT_MS = 10_000L
    }
}
