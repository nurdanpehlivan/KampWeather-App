package com.nurdan.hava.data

import com.nurdan.hava.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * İnternete giden bütün isteklerin tek kurulum noktası (singleton).
 *
 * İki sunucu var: şehir arama `geocoding-api.open-meteo.com`, tahmin
 * `api.open-meteo.com`. İkisi de aynı [OkHttpClient]'ı paylaşır; bağlantı havuzu
 * tek kez kurulur.
 */
object ApiClient {

    private const val GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/"
    private const val FORECAST_BASE_URL = "https://api.open-meteo.com/"

    /**
     * Debug derlemesinde her istek ve yanıt Logcat'e düşer
     * (filtre: `okhttp.OkHttpClient`). Release derlemesinde günlük kapalıdır.
     */
    private val httpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        }
        OkHttpClient.Builder().addInterceptor(logging).build()
    }

    /** Şehir arama istemcisi. */
    val cityApi: CityApi by lazy { createApi(GEOCODING_BASE_URL) }

    /** Hava tahmini istemcisi. */
    val forecastApi: ForecastApi by lazy { createApi(FORECAST_BASE_URL) }

    private inline fun <reified T> createApi(baseUrl: String): T = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(T::class.java)
}
