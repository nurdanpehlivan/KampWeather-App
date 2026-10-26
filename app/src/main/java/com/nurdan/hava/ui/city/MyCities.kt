package com.nurdan.hava.ui.city

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nurdan.hava.R
import com.nurdan.hava.model.City
import com.nurdan.hava.model.CurrentWeather
import com.nurdan.hava.model.DailyForecast
import com.nurdan.hava.model.Forecast
import com.nurdan.hava.model.MascotMood
import com.nurdan.hava.model.WeatherCondition
import com.nurdan.hava.ui.LoadState
import com.nurdan.hava.ui.mascot.CloudMascot
import com.nurdan.hava.ui.theme.HavaTheme
import com.nurdan.hava.ui.theme.glass
import com.nurdan.hava.ui.theme.isNightAt
import com.nurdan.hava.ui.theme.skyBrush
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.roundToInt

/**
 * Arama kutusu boşken görünen "Şehirlerim" listesi.
 *
 * Her kart, o şehrin o anki gökyüzü renginde boyanır; bir bakışta hangi şehirde
 * havanın nasıl olduğu görülür. Favori kartı sola kaydırılınca listeden çıkar,
 * liste aşağı çekilince bütün kartlar tazelenir.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCitiesList(
    cities: List<CityWeather>,
    locating: Boolean,
    onLocateClick: () -> Unit,
    onOpen: (City) -> Unit,
    onRemove: (City) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasLocation = cities.any { it.isMyLocation }
    PullToRefreshBox(
        isRefreshing = cities.any { it.weather == LoadState.Loading },
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!hasLocation) {
                item(key = "locationPrompt") {
                    LocationPrompt(locating = locating, onLocateClick = onLocateClick)
                }
            }
            items(items = cities, key = { "my-${it.city.id}" }) { item ->
                if (item.isMyLocation) {
                    WeatherCityCard(
                        item = item,
                        onClick = { onOpen(item.city) },
                        trailing = {
                            IconButton(onClick = onLocateClick, enabled = !locating, modifier = Modifier.size(32.dp)) {
                                if (locating) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                                } else {
                                    Icon(
                                        Icons.Filled.Refresh,
                                        contentDescription = stringResource(R.string.my_location),
                                        tint = Color.White
                                    )
                                }
                            }
                        },
                        modifier = Modifier.animateItem()
                    )
                } else {
                    SwipeToRemove(onRemove = { onRemove(item.city) }, modifier = Modifier.animateItem()) {
                        WeatherCityCard(item = item, onClick = { onOpen(item.city) })
                    }
                }
            }
            item(key = "hint") {
                Text(
                    text = stringResource(
                        if (cities.none {
                                !it.isMyLocation
                            }
                        ) {
                            R.string.my_cities_empty_hint
                        } else {
                            R.string.my_cities_swipe_hint
                        }
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 8.dp, end = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToRemove(onRemove: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    @Suppress("DEPRECATION")
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) onRemove()
            value == SwipeToDismissBoxValue.EndToStart
        }
    )
    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        modifier = modifier,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0x66B3261E))
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.action_favorite_remove), color = Color.White, style = MaterialTheme.typography.labelLarge)
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = Color.White)
                }
            }
        }
    ) { content() }
}

/**
 * Bir şehrin anlık havası, o havanın gökyüzü renginde.
 * Solda ad ve durum, sağda büyük ince sıcaklık ve küçük maskot.
 */
@Composable
fun WeatherCityCard(item: CityWeather, onClick: () -> Unit, modifier: Modifier = Modifier, trailing: @Composable (() -> Unit)? = null) {
    val forecast = (item.weather as? LoadState.Success)?.data
    val condition = forecast?.let { WeatherCondition.fromWeatherCode(it.current.weatherCode) } ?: WeatherCondition.UNKNOWN
    val night = forecast?.let { isNightAt(it.current.observedAt) } ?: isNightAt(LocalDateTime.now())
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(skyBrush(condition, night))
            .border(1.dp, Color(0x38FFFFFF), shape)
            .clickable(onClick = onClick)
            .padding(start = 20.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                if (item.isMyLocation) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.my_location).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                        trailing?.let {
                            Spacer(Modifier.width(4.dp))
                            it()
                        }
                    }
                }
                Text(
                    text = item.city.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = when (val w = item.weather) {
                        is LoadState.Error -> w.message
                        LoadState.Loading -> "Yükleniyor…"
                        else -> condition.label
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Spacer(Modifier.height(14.dp))
                forecast?.daily?.firstOrNull()?.let {
                    Text(
                        text = "↑ ${it.maxTemperatureC.roundToInt()}°   ↓ ${it.minTemperatureC.roundToInt()}°",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = forecast?.let { "${it.current.temperatureC.roundToInt()}°" } ?: "–",
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White
                )
                CloudMascot(mood = MascotMood.from(condition, night), size = 44.dp)
            }
        }
    }
}

/** Konum henüz yokken ilk kart: tek dokunuşla bulunduğun yerin havası. */
@Composable
private fun LocationPrompt(locating: Boolean, onLocateClick: () -> Unit) {
    val shape = RoundedCornerShape(28.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glass(shape, strong = true)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CloudMascot(mood = MascotMood.SUNNY, size = 56.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.location_prompt_title), style = MaterialTheme.typography.titleMedium, color = Color.White)
            Text(
                stringResource(R.string.location_prompt_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onLocateClick,
                enabled = !locating,
                shape = CircleShape,
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding
            ) {
                if (locating) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                }
                Text(
                    text = stringResource(if (locating) R.string.locating else R.string.use_my_location),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF2F80ED)
@Composable
private fun MyCitiesPreview() {
    fun forecast(code: Int, temp: Double, hour: Int) = Forecast(
        current = CurrentWeather(temp, code, LocalDateTime.of(2026, 10, 26, hour, 0)),
        hourly = emptyList(),
        daily = listOf(DailyForecast(LocalDate.of(2026, 10, 26), temp - 5, temp + 3, code))
    )
    HavaTheme {
        MyCitiesList(
            cities = listOf(
                CityWeather(City(-1, "Çankaya", 39.9, 32.8, "TR", "Türkiye", "Ankara"), true, LoadState.Success(forecast(0, 21.0, 13))),
                CityWeather(
                    City(745044, "İstanbul", 41.0, 28.9, "TR", "Türkiye", "İstanbul"),
                    false,
                    LoadState.Success(forecast(61, 16.0, 13))
                ),
                CityWeather(City(53, "Rize", 41.0, 40.5, "TR", "Türkiye", "Rize"), false, LoadState.Success(forecast(95, 14.0, 22)))
            ),
            locating = false,
            onLocateClick = {},
            onOpen = {},
            onRemove = {},
            onRefresh = {}
        )
    }
}
