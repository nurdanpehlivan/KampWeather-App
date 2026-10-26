package com.nurdan.hava.ui.city

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nurdan.hava.R
import com.nurdan.hava.model.City
import com.nurdan.hava.model.MascotMood
import com.nurdan.hava.model.WeatherCondition
import com.nurdan.hava.ui.LoadState
import com.nurdan.hava.ui.component.EmptyView
import com.nurdan.hava.ui.component.ErrorView
import com.nurdan.hava.ui.component.SkeletonList
import com.nurdan.hava.ui.mascot.CloudMascot
import com.nurdan.hava.ui.theme.HavaTheme
import com.nurdan.hava.ui.theme.isNightAt
import com.nurdan.hava.ui.theme.sky
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * ViewModel'i edinip state'i [CityScreen]'e aktaran bağlayıcı katman.
 * Konum izni ve konum sonucuna göre gezinme de burada yönetilir.
 */
@Composable
fun CityRoute(onCityClick: (City) -> Unit, modifier: Modifier = Modifier, viewModel: CityViewModel = viewModel()) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedCityId by viewModel.selectedCityId.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val myCities by viewModel.myCities.collectAsStateWithLifecycle()
    val locationState by viewModel.location.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val openCity: (City) -> Unit = { city ->
        viewModel.onCitySelected(city)
        onCityClick(city)
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            viewModel.onLocate()
        } else {
            Toast.makeText(context, R.string.location_denied, Toast.LENGTH_SHORT).show()
        }
    }

    // Konum bulununca ana ekrandaki ilk kart olur; bulunamazsa kısa bir uyarı.
    LaunchedEffect(locationState) {
        if (locationState == LocationState.Failed) {
            Toast.makeText(context, R.string.location_failed, Toast.LENGTH_LONG).show()
        }
        if (locationState is LocationState.Found || locationState == LocationState.Failed) {
            viewModel.onLocationHandled()
        }
    }

    CityScreen(
        query = query,
        uiState = uiState,
        selectedCityId = selectedCityId,
        favoriteIds = favorites.map { it.id }.toSet(),
        myCities = myCities,
        locating = locationState == LocationState.Searching,
        onQueryChange = viewModel::onQueryChange,
        onSearch = viewModel::search,
        onCityClick = openCity,
        onToggleFavorite = viewModel::onToggleFavorite,
        onRefreshMyCities = viewModel::onRefreshMyCities,
        onLocateClick = {
            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
            if (granted) viewModel.onLocate() else permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        },
        modifier = modifier
    )
}

/**
 * Ana ekran. Zemini, listedeki ilk şehrin o anki gökyüzüdür (yoksa saate göre açık gökyüzü).
 *
 * Arama kutusu boşken "Şehirlerim" kartları, bir şey yazılınca arama sonuçları görünür.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityScreen(
    query: String,
    uiState: LoadState<List<City>>,
    selectedCityId: Long?,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onCityClick: (City) -> Unit,
    modifier: Modifier = Modifier,
    favoriteIds: Set<Long> = emptySet(),
    myCities: List<CityWeather> = emptyList(),
    locating: Boolean = false,
    onToggleFavorite: (City) -> Unit = {},
    onRefreshMyCities: () -> Unit = {},
    onLocateClick: () -> Unit = {}
) {
    val home = myCities.firstNotNullOfOrNull { (it.weather as? LoadState.Success)?.data }
    val condition = home?.let { WeatherCondition.fromWeatherCode(it.current.weatherCode) } ?: WeatherCondition.CLEAR
    val night = home?.let { isNightAt(it.current.observedAt) } ?: isNightAt(LocalDateTime.now())
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier
            .sky(condition, night)
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge)
                        Text(
                            text = todayLine(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    CloudMascot(mood = MascotMood.from(condition, night), size = 64.dp, modifier = Modifier.padding(end = 12.dp))
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    titleContentColor = Color.White
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SearchField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = onSearch,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 8.dp)
            )
            if (query.isBlank()) {
                MyCitiesList(
                    cities = myCities,
                    locating = locating,
                    onLocateClick = onLocateClick,
                    onOpen = onCityClick,
                    onRemove = onToggleFavorite,
                    onRefresh = onRefreshMyCities
                )
            } else {
                SearchResults(
                    uiState = uiState,
                    selectedCityId = selectedCityId,
                    favoriteIds = favoriteIds,
                    onSearch = onSearch,
                    onCityClick = onCityClick,
                    onToggleFavorite = onToggleFavorite
                )
            }
        }
    }
}

/** Arama sonucunun dört durumu; durumlar arasında yumuşak bir solma ile geçilir. */
@Composable
private fun SearchResults(
    uiState: LoadState<List<City>>,
    selectedCityId: Long?,
    favoriteIds: Set<Long>,
    onSearch: () -> Unit,
    onCityClick: (City) -> Unit,
    onToggleFavorite: (City) -> Unit
) {
    AnimatedContent(
        targetState = uiState,
        contentKey = { it::class },
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "cityState",
        modifier = Modifier.fillMaxSize()
    ) { state ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (state) {
                LoadState.Loading -> SkeletonList()
                is LoadState.Success -> CityList(
                    cities = state.data,
                    selectedCityId = selectedCityId,
                    favoriteIds = favoriteIds,
                    onCityClick = onCityClick,
                    onToggleFavorite = onToggleFavorite
                )
                LoadState.Empty -> EmptyView(
                    title = stringResource(R.string.city_empty_title),
                    message = stringResource(R.string.city_empty_message)
                )
                is LoadState.Error -> ErrorView(message = state.message, onRetry = onSearch)
            }
        }
    }
}

/**
 * Cam görünümlü, hap biçimli arama kutusu. Yazdıkça arama ViewModel'de kendiliğinden
 * başlar; klavyedeki "Ara" tuşu beklemeden hemen arar.
 */
@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit, onSearch: () -> Unit, modifier: Modifier = Modifier) {
    val keyboardController = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(R.string.search_hint)) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.action_clear))
                }
            }
        },
        singleLine = true,
        shape = CircleShape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedBorderColor = Color.White,
            cursorColor = Color.White,
            focusedLeadingIconColor = Color.White,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedTrailingIconColor = Color.White,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                keyboardController?.hide()
                onSearch()
            }
        )
    )
}

/** Sonuç sayısı başlığı + şehir kartları. */
@Composable
private fun CityList(
    cities: List<City>,
    selectedCityId: Long?,
    favoriteIds: Set<Long>,
    onCityClick: (City) -> Unit,
    onToggleFavorite: (City) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "count") {
            Text(
                text = pluralStringResource(R.plurals.city_result_count, cities.size, cities.size).uppercase(TURKISH),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )
        }
        items(items = cities, key = { it.id }) { city ->
            CityCard(
                city = city,
                selected = city.id == selectedCityId,
                onClick = { onCityClick(city) },
                isFavorite = city.id in favoriteIds,
                onToggleFavorite = { onToggleFavorite(city) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

private val TURKISH: Locale = Locale.forLanguageTag("tr")

/** "26 EKİM PAZARTESİ". */
private fun todayLine(): String = LocalDate.now()
    .format(DateTimeFormatter.ofPattern("d MMMM EEEE", TURKISH))
    .uppercase(TURKISH)

@Preview(showBackground = true)
@Composable
private fun CityScreenSearchPreview() {
    HavaTheme {
        CityScreen(
            query = "Ankara",
            uiState = LoadState.Success(
                listOf(
                    City(323786, "Ankara", 39.91987, 32.85427, "TR", "Türkiye", "Ankara"),
                    City(745044, "İstanbul", 41.01384, 28.94966, "TR", "Türkiye", "İstanbul"),
                    City(2950159, "Berlin", 52.52437, 13.41053, "DE", "Almanya", "Berlin")
                )
            ),
            selectedCityId = 323786,
            favoriteIds = setOf(745044),
            onQueryChange = {},
            onSearch = {},
            onCityClick = {}
        )
    }
}
