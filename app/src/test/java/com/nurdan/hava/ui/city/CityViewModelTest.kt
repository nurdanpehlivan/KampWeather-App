package com.nurdan.hava.ui.city

import com.nurdan.hava.testing.FakeCityRepository
import com.nurdan.hava.testing.FakeLocationRepository
import com.nurdan.hava.testing.FakeSavedCitiesRepository
import com.nurdan.hava.testing.FakeWeatherRepository
import com.nurdan.hava.testing.MainDispatcherRule
import com.nurdan.hava.testing.sampleCity
import com.nurdan.hava.ui.LoadState
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

/**
 * Ana ekranın iki modunu doğrular:
 * - Arama: her istek Loading ile başlar ve Success / Empty / Error'dan biriyle biter.
 * - Şehirlerim: konum şehri + favoriler, her biri kendi havasıyla.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CityViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeCityRepository()
    private val savedCities = FakeSavedCitiesRepository()
    private val location = FakeLocationRepository()
    private val weather = FakeWeatherRepository()

    private fun createViewModel() = CityViewModel(repository, savedCities, location, weather)

    /** Kullanıcının bir şey yazıp klavyede "Ara"ya basması. */
    private fun TestScope.searchFor(viewModel: CityViewModel, text: String) {
        viewModel.onQueryChange(text)
        viewModel.search()
        advanceUntilIdle()
    }

    // --- Arama ----------------------------------------------------------

    @Test
    fun `acilista arama yapilmaz ve kutu bostur`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("", viewModel.query.value)
        assertEquals(0, repository.callCount)
    }

    @Test
    fun `arama baslarken Loading gosterilir`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onQueryChange("Ankara")
        viewModel.search()

        // advanceUntilIdle çağrılmadan önce istek henüz bitmemiştir.
        assertEquals(LoadState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `veri gelince Success durumuna gecilir`() = runTest {
        repository.result = listOf(sampleCity(), sampleCity(id = 2L, name = "İzmir"))
        val viewModel = createViewModel()

        searchFor(viewModel, "İ")

        val state = viewModel.uiState.value
        assertTrue("Success bekleniyordu, gelen: $state", state is LoadState.Success)
        assertEquals(2, (state as LoadState.Success).data.size)
    }

    @Test
    fun `bos liste Empty durumuna cevrilir`() = runTest {
        repository.result = emptyList()
        val viewModel = createViewModel()

        searchFor(viewModel, "xyz")

        assertEquals(LoadState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `baglanti hatasi anlasilir mesaja cevrilir`() = runTest {
        repository.error = IOException("no network")
        val viewModel = createViewModel()

        searchFor(viewModel, "Ankara")

        val state = viewModel.uiState.value
        assertTrue("Error bekleniyordu, gelen: $state", state is LoadState.Error)
        assertEquals("İnternet bağlantını kontrol et.", (state as LoadState.Error).message)
    }

    @Test
    fun `sunucu hatasi farkli mesaj gosterir`() = runTest {
        repository.error = HttpException(
            Response.error<Unit>(500, "".toResponseBody("application/json".toMediaType()))
        )
        val viewModel = createViewModel()

        searchFor(viewModel, "Ankara")

        assertEquals("Sunucu şu an cevap vermiyor.", (viewModel.uiState.value as LoadState.Error).message)
    }

    @Test
    fun `yazdikca kisa bir bekleme sonrasi tek istek atilir`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Kullanıcı hızlı yazıyor: her harfte istek atılmamalı.
        viewModel.onQueryChange("R")
        viewModel.onQueryChange("Ri")
        viewModel.onQueryChange("Riz")
        viewModel.onQueryChange("Rize")
        advanceUntilIdle()

        assertEquals(1, repository.callCount)
        assertEquals("Rize", repository.lastQuery)
    }

    @Test
    fun `tek harfle otomatik arama yapilmaz`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onQueryChange("R")
        advanceUntilIdle()

        assertEquals(0, repository.callCount)
    }

    @Test
    fun `bos metinle arama yapilmaz`() = runTest {
        val viewModel = createViewModel()

        searchFor(viewModel, "   ")

        assertEquals(0, repository.callCount)
    }

    @Test
    fun `tekrar dene yeni bir istek baslatir ve basarili olabilir`() = runTest {
        repository.error = IOException("no network")
        val viewModel = createViewModel()
        searchFor(viewModel, "Ankara")
        assertTrue(viewModel.uiState.value is LoadState.Error)

        // Bağlantı geri geldi.
        repository.error = null
        repository.result = listOf(sampleCity())
        viewModel.search()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is LoadState.Success)
    }

    // --- Seçim ve favoriler ---------------------------------------------

    @Test
    fun `sehre dokununca secilir ve son bakilan olarak kaydedilir`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onCitySelected(sampleCity(id = 42L, name = "Rize"))
        advanceUntilIdle()

        assertEquals(42L, viewModel.selectedCityId.value)
        assertEquals("Rize", savedCities.lastViewed?.name)
    }

    @Test
    fun `favori eklenir ve ikinci dokunusta cikarilir`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onToggleFavorite(sampleCity())
        advanceUntilIdle()
        assertEquals(listOf(sampleCity()), viewModel.favorites.value)

        viewModel.onToggleFavorite(sampleCity())
        advanceUntilIdle()
        assertTrue(viewModel.favorites.value.isEmpty())
    }

    // --- Şehirlerim -----------------------------------------------------

    @Test
    fun `favoriler havasiyla birlikte listelenir`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onToggleFavorite(sampleCity(id = 1L, name = "Ankara"))
        viewModel.onToggleFavorite(sampleCity(id = 2L, name = "Rize"))
        advanceUntilIdle()

        val cards = viewModel.myCities.value
        assertEquals(listOf("Ankara", "Rize"), cards.map { it.city.name })
        assertTrue(cards.all { it.weather is LoadState.Success })
        assertFalse(cards.any { it.isMyLocation })
    }

    @Test
    fun `konum sehri ilk karttir ve favorilerde tekrar etmez`() = runTest {
        location.result = sampleCity(id = 7L, name = "Çankaya")
        val viewModel = createViewModel()
        viewModel.onToggleFavorite(sampleCity(id = 1L, name = "Ankara"))
        viewModel.onToggleFavorite(sampleCity(id = 7L, name = "Çankaya"))
        advanceUntilIdle()

        viewModel.onLocate()
        advanceUntilIdle()

        val cards = viewModel.myCities.value
        assertEquals(listOf("Çankaya", "Ankara"), cards.map { it.city.name })
        assertTrue(cards.first().isMyLocation)
    }

    @Test
    fun `kart havasi alinamazsa o kart Error olur`() = runTest {
        weather.error = IOException("no network")
        val viewModel = createViewModel()
        viewModel.onToggleFavorite(sampleCity())
        advanceUntilIdle()

        assertTrue(viewModel.myCities.value.single().weather is LoadState.Error)
    }

    @Test
    fun `yenile butun kartlarin havasini yeniden ister`() = runTest {
        val viewModel = createViewModel()
        viewModel.onToggleFavorite(sampleCity())
        advanceUntilIdle()
        val before = weather.callCount

        viewModel.onRefreshMyCities()
        advanceUntilIdle()

        assertEquals(before + 1, weather.callCount)
    }

    // --- Konum ----------------------------------------------------------

    @Test
    fun `konum bulununca Found durumuna gecilir ve kaydedilir`() = runTest {
        location.result = sampleCity(id = -1L, name = "Çankaya")
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onLocate()
        advanceUntilIdle()

        assertEquals(LocationState.Found(sampleCity(id = -1L, name = "Çankaya")), viewModel.location.value)
        assertEquals("Çankaya", savedCities.widgetCity()?.name)

        viewModel.onLocationHandled()
        assertEquals(LocationState.Idle, viewModel.location.value)
    }

    @Test
    fun `konum alinamazsa Failed durumuna gecilir`() = runTest {
        location.result = null
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onLocate()
        advanceUntilIdle()

        assertEquals(LocationState.Failed, viewModel.location.value)
    }
}
