package com.nurdan.hava.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.nurdan.hava.model.City
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Kullanıcının kaydettiği şehirler: favoriler ve en son açılan şehir.
 *
 * Uygulama kapanıp açılsa da kaybolmaz. Ana ekran widget'ı da hangi şehri
 * göstereceğini buradan öğrenir.
 */
interface SavedCitiesRepository {
    val favorites: Flow<List<City>>

    /** Favoriyse çıkarır, değilse ekler. */
    suspend fun toggleFavorite(city: City)

    suspend fun setLastViewed(city: City)

    /** "Konumum" ile bulunan şehir; ana ekranda ilk kart. */
    val locationCity: Flow<City?>

    suspend fun setLocationCity(city: City)

    /** Widget'ın göstereceği şehir: konum şehri, yoksa ilk favori, yoksa en son bakılan şehir. */
    suspend fun widgetCity(): City?
}

private val Context.savedCitiesStore: DataStore<Preferences> by preferencesDataStore(name = "saved_cities")

/**
 * DataStore uygulaması. Şehirler JSON olarak tek bir anahtarda saklanır; Gson
 * zaten projede olduğu için ayrı bir serileştirici eklemeye gerek yok.
 *
 * @param onChanged Favoriler değişince çağrılır (widget'ı tazelemek için).
 */
class DataStoreSavedCitiesRepository(
    context: Context,
    private val onChanged: suspend () -> Unit = {}
) : SavedCitiesRepository {

    private val store = context.applicationContext.savedCitiesStore
    private val gson = Gson()
    private val listType = object : TypeToken<List<City>>() {}.type

    override val favorites: Flow<List<City>> = store.data.map { decodeList(it[FAVORITES]) }

    override suspend fun toggleFavorite(city: City) {
        store.edit { prefs ->
            val current = decodeList(prefs[FAVORITES])
            val updated = if (current.any { it.id == city.id }) {
                current.filterNot { it.id == city.id }
            } else {
                current + city
            }
            prefs[FAVORITES] = gson.toJson(updated)
        }
        onChanged()
    }

    override suspend fun setLastViewed(city: City) {
        store.edit { it[LAST_VIEWED] = gson.toJson(city) }
        onChanged()
    }

    override val locationCity: Flow<City?> = store.data.map { prefs -> prefs[LOCATION]?.let(::decodeCity) }

    override suspend fun setLocationCity(city: City) {
        store.edit { it[LOCATION] = gson.toJson(city) }
        onChanged()
    }

    override suspend fun widgetCity(): City? {
        val prefs = store.data.first()
        return prefs[LOCATION]?.let(::decodeCity)
            ?: decodeList(prefs[FAVORITES]).firstOrNull()
            ?: prefs[LAST_VIEWED]?.let(::decodeCity)
    }

    /** Bozuk ya da eski biçimli veri uygulamayı çökertmez; boş liste sayılır. */
    private fun decodeList(json: String?): List<City> = try {
        json?.let { gson.fromJson<List<City>>(it, listType) }.orEmpty()
    } catch (e: JsonSyntaxException) {
        emptyList()
    }

    private fun decodeCity(json: String): City? = try {
        gson.fromJson(json, City::class.java)
    } catch (e: JsonSyntaxException) {
        null
    }

    private companion object {
        val FAVORITES = stringPreferencesKey("favorites")
        val LAST_VIEWED = stringPreferencesKey("last_viewed")
        val LOCATION = stringPreferencesKey("location_city")
    }
}
