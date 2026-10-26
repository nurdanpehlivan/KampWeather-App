package com.nurdan.hava.ui.city

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nurdan.hava.R
import com.nurdan.hava.model.City
import com.nurdan.hava.ui.LoadState
import com.nurdan.hava.ui.component.EmptyView
import com.nurdan.hava.ui.component.ErrorView
import com.nurdan.hava.ui.component.SkeletonList
import com.nurdan.hava.ui.theme.HavaTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** ViewModel'i edinip state'i [CityScreen]'e aktaran bağlayıcı katman. */
@Composable
fun CityRoute(onCityClick: (City) -> Unit, modifier: Modifier = Modifier, viewModel: CityViewModel = viewModel()) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedCityId by viewModel.selectedCityId.collectAsStateWithLifecycle()

    CityScreen(
        query = query,
        uiState = uiState,
        selectedCityId = selectedCityId,
        onQueryChange = viewModel::onQueryChange,
        onSearch = viewModel::search,
        onCityClick = { city ->
            viewModel.onCitySelected(city)
            onCityClick(city)
        },
        modifier = modifier
    )
}

/**
 * Ana ekran: büyük serif başlık, hap biçimli arama kutusu ve şehir listesi.
 * Başlık liste kaydırıldıkça küçülür.
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
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
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
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
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
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
            // Durumlar arasında sert geçiş yerine yumuşak bir solma.
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
                            onCityClick = onCityClick
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
    }
}

/**
 * Hap biçimli arama kutusu. Yazdıkça arama ViewModel'de kendiliğinden başlar;
 * klavyedeki "Ara" tuşu beklemeden hemen arar.
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
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary
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
private fun CityList(cities: List<City>, selectedCityId: Long?, onCityClick: (City) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "count") {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = pluralStringResource(R.plurals.city_result_count, cities.size, cities.size)
                        .uppercase(TURKISH),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        items(items = cities, key = { it.id }) { city ->
            CityCard(
                city = city,
                selected = city.id == selectedCityId,
                onClick = { onCityClick(city) },
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
private fun CityScreenPreview() {
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
            onQueryChange = {},
            onSearch = {},
            onCityClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CityScreenEmptyNightPreview() {
    HavaTheme(darkTheme = true) {
        CityScreen(
            query = "xyz",
            uiState = LoadState.Empty,
            selectedCityId = null,
            onQueryChange = {},
            onSearch = {},
            onCityClick = {}
        )
    }
}
