package com.nurdan.hava

import android.app.Application
import android.content.Context
import androidx.glance.appwidget.updateAll
import com.nurdan.hava.data.AndroidLocationRepository
import com.nurdan.hava.data.DataStoreSavedCitiesRepository
import com.nurdan.hava.data.LocationRepository
import com.nurdan.hava.data.SavedCitiesRepository
import com.nurdan.hava.ui.widget.HavaWidget

/**
 * Context gerektiren bağımlılıkların tek kurulum yeri (basit bir servis bulucu).
 *
 * ViewModel'ler `viewModel()` ile parametresiz kurulduğu için Context'e doğrudan
 * erişemez; varsayılan değer olarak buradaki örnekleri kullanırlar. Testler ise
 * kendi sahtelerini verir.
 */
object AppGraph {
    lateinit var savedCities: SavedCitiesRepository
        private set
    lateinit var location: LocationRepository
        private set

    fun init(context: Context) {
        if (::savedCities.isInitialized) return
        val app = context.applicationContext
        // Favoriler değişince ana ekran widget'ı da hemen tazelenir.
        savedCities = DataStoreSavedCitiesRepository(app) { HavaWidget().updateAll(app) }
        location = AndroidLocationRepository(app)
    }
}

/** Uygulama açılırken [AppGraph]'ı kurar. */
class HavaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppGraph.init(this)
    }
}
