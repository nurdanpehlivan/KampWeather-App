package com.nurdan.hava.ui.forecast

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nurdan.hava.R
import com.nurdan.hava.model.CurrentWeather
import com.nurdan.hava.model.DailyForecast
import com.nurdan.hava.model.Forecast
import com.nurdan.hava.model.HourlyForecast
import com.nurdan.hava.model.WeatherCondition
import com.nurdan.hava.ui.LoadState
import com.nurdan.hava.ui.component.EmptyView
import com.nurdan.hava.ui.component.ErrorView
import com.nurdan.hava.ui.theme.HavaTheme
import com.nurdan.hava.ui.theme.onSky
import com.nurdan.hava.ui.theme.skyBrush
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToInt

/** ViewModel'i edinip state'i [ForecastScreen]'e aktaran bağlayıcı katman. */
@Composable
fun ForecastRoute(onBack: () -> Unit, modifier: Modifier = Modifier, viewModel: ForecastViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ForecastScreen(
        cityName = viewModel.cityName,
        subtitle = viewModel.subtitle,
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::onRetry,
        modifier = modifier
    )
}

/**
 * Bir şehrin hava tahmini.
 *
 * Üstte, havanın durumuna ve günün saatine göre renklenen bir "gökyüzü" bloğu;
 * altında üç ölçüm kutusu, saatlik sıcaklık sütunları ve haftalık sıcaklık aralığı çubukları.
 */
@Composable
fun ForecastScreen(
    cityName: String,
    subtitle: String,
    uiState: LoadState<Forecast>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = cityName.ifEmpty { stringResource(R.string.detail_title) }
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (uiState) {
            is LoadState.Success -> ForecastContent(
                cityName = title,
                subtitle = subtitle,
                forecast = uiState.data,
                onBack = onBack
            )
            else -> Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                PlainHeader(title = title, subtitle = subtitle, onBack = onBack)
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    when (uiState) {
                        LoadState.Loading -> CircularProgressIndicator(strokeCap = StrokeCap.Round)
                        LoadState.Empty -> EmptyView(title = stringResource(R.string.detail_empty), symbol = "🛰️")
                        is LoadState.Error -> ErrorView(message = uiState.message, onRetry = onRetry)
                        is LoadState.Success -> Unit
                    }
                }
            }
        }
    }
}

@Composable
private fun PlainHeader(title: String, subtitle: String, onBack: () -> Unit) {
    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        BackButton(onBack = onBack, tint = MaterialTheme.colorScheme.onSurface, container = MaterialTheme.colorScheme.surfaceVariant)
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            if (subtitle.isNotEmpty()) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun BackButton(onBack: () -> Unit, tint: Color, container: Color) {
    IconButton(onClick = onBack) {
        Surface(shape = CircleShape, color = container) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.action_back),
                tint = tint,
                modifier = Modifier.padding(8.dp).size(20.dp)
            )
        }
    }
}

@Composable
private fun ForecastContent(cityName: String, subtitle: String, forecast: Forecast, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SkyHeader(
            cityName = cityName,
            subtitle = subtitle,
            current = forecast.current,
            today = forecast.daily.firstOrNull(),
            onBack = onBack
        )
        MetricTiles(current = forecast.current, modifier = Modifier.padding(horizontal = 20.dp))
        SectionLabel(stringResource(R.string.detail_hourly))
        HourlyColumns(hours = forecast.hourly)
        SectionLabel(stringResource(R.string.detail_daily))
        WeekCard(days = forecast.daily, modifier = Modifier.padding(horizontal = 20.dp))
    }
}

/** Gökyüzü bloğu: durum çubuğunun altına kadar uzanan, alt köşeleri yuvarlatılmış gradyan. */
@Composable
private fun SkyHeader(cityName: String, subtitle: String, current: CurrentWeather, today: DailyForecast?, onBack: () -> Unit) {
    val condition = WeatherCondition.fromWeatherCode(current.weatherCode)
    val night = isNight(current.observedAt)
    val ink = onSky(condition, night)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(skyBrush(condition, night), RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            .statusBarsPadding()
            .padding(start = 8.dp, end = 24.dp, top = 8.dp, bottom = 32.dp)
    ) {
        BackButton(onBack = onBack, tint = ink, container = ink.copy(alpha = 0.18f))
        Column(modifier = Modifier.padding(start = 16.dp, top = 12.dp)) {
            Text(text = cityName, style = MaterialTheme.typography.headlineMedium, color = ink)
            if (subtitle.isNotEmpty()) {
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = ink.copy(alpha = 0.8f))
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp)) {
                Text(
                    text = degrees(current.temperatureC),
                    style = MaterialTheme.typography.displayLarge,
                    color = ink,
                    modifier = Modifier.weight(1f)
                )
                Text(text = condition.emoji, style = MaterialTheme.typography.displayMedium)
            }
            Text(text = condition.label, style = MaterialTheme.typography.titleLarge, color = ink)
            today?.let {
                Text(
                    text = "En yüksek ${degrees(it.maxTemperatureC)}  ·  En düşük ${degrees(it.minTemperatureC)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = ink.copy(alpha = 0.85f),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

/** Hissedilen · nem · rüzgâr; servis göndermediyse o kutu çizilmez. */
@Composable
private fun MetricTiles(current: CurrentWeather, modifier: Modifier = Modifier) {
    val tiles = listOfNotNull(
        current.apparentTemperatureC?.let { Triple("🌡️", stringResource(R.string.detail_feels_like), degrees(it)) },
        current.humidityPercent?.let { Triple("💧", stringResource(R.string.detail_humidity), "%$it") },
        current.windSpeedKmh?.let { Triple("🍃", stringResource(R.string.detail_wind), "${it.roundToInt()} km/sa") }
    )
    if (tiles.isEmpty()) return
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        tiles.forEach { (symbol, label, value) ->
            Surface(
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(symbol, style = MaterialTheme.typography.titleLarge)
                    Text(value, style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = label.uppercase(TURKISH),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Önümüzdeki 24 saat; her saat bir sütun. Sütundaki çubuğun boyu o saatin sıcaklığını
 * gösterir, böylece günün ısınıp soğuması bir bakışta okunur. İlk sütun "Şimdi"dir.
 */
@Composable
private fun HourlyColumns(hours: List<HourlyForecast>) {
    val now = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS)
    val upcoming = hours.filter { !it.time.isBefore(now) }.take(HOURLY_COUNT)
    if (upcoming.isEmpty()) return
    val min = upcoming.minOf { it.temperatureC }
    val max = upcoming.maxOf { it.temperatureC }
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(items = upcoming, key = { _, hour -> hour.time.toString() }) { index, hour ->
            val first = index == 0
            val container = if (first) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            val content = if (first) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            Surface(shape = CircleShape, color = container, contentColor = content, modifier = Modifier.width(62.dp)) {
                Column(
                    modifier = Modifier.padding(vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (first) stringResource(R.string.now) else hour.time.format(HOUR_FORMAT),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(WeatherCondition.fromWeatherCode(hour.weatherCode).emoji, style = MaterialTheme.typography.titleLarge)
                    TemperatureStem(fraction = fraction(hour.temperatureC, min, max), color = content)
                    Text(degrees(hour.temperatureC), style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = percent(hour.precipitationProbability) ?: " ",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (first) content else MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

/** Sabit yükseklikte bir kutu; içindeki çubuk alttan [fraction] oranında dolar. */
@Composable
private fun TemperatureStem(fraction: Float, color: Color) {
    Canvas(modifier = Modifier.size(width = 6.dp, height = 44.dp)) {
        val radius = CornerRadius(size.width / 2)
        drawRoundRect(color = color.copy(alpha = 0.18f), cornerRadius = radius)
        val filled = size.height * (0.2f + 0.8f * fraction)
        drawRoundRect(
            color = color,
            topLeft = Offset(0f, size.height - filled),
            size = Size(size.width, filled),
            cornerRadius = radius
        )
    }
}

/** Haftalık liste: her gün için haftanın en soğuğundan en sıcağına uzanan bir aralık çubuğu. */
@Composable
private fun WeekCard(days: List<DailyForecast>, modifier: Modifier = Modifier) {
    if (days.isEmpty()) return
    val weekMin = days.minOf { it.minTemperatureC }
    val weekMax = days.maxOf { it.maxTemperatureC }
    Surface(modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            days.forEachIndexed { index, day ->
                DayRow(day = day, isToday = index == 0, weekMin = weekMin, weekMax = weekMax)
            }
        }
    }
}

@Composable
private fun DayRow(day: DailyForecast, isToday: Boolean, weekMin: Double, weekMax: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.width(78.dp)) {
            Text(
                text = if (isToday) stringResource(R.string.today) else day.date.format(DAY_FORMAT).replaceFirstChar(Char::titlecase),
                style = MaterialTheme.typography.titleMedium
            )
            percent(day.precipitationProbability)?.let {
                Text("💧 $it", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            }
        }
        Text(WeatherCondition.fromWeatherCode(day.weatherCode).emoji, style = MaterialTheme.typography.titleLarge)
        Text(
            text = degrees(day.minTemperatureC),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.width(44.dp)
        )
        RangeBar(
            start = fraction(day.minTemperatureC, weekMin, weekMax),
            end = fraction(day.maxTemperatureC, weekMin, weekMax),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp)
        )
        Text(
            text = degrees(day.maxTemperatureC),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.width(36.dp)
        )
    }
}

/** Soluk bir ray üzerinde, soğuktan (çivit) sıcağa (mercan) giden renkli bir parça. */
@Composable
private fun RangeBar(start: Float, end: Float, modifier: Modifier = Modifier) {
    val track = MaterialTheme.colorScheme.outline
    val cold = MaterialTheme.colorScheme.secondary
    val warm = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier.height(6.dp)) {
        val radius = CornerRadius(size.height / 2)
        drawRoundRect(color = track, cornerRadius = radius)
        val left = size.width * start
        val width = (size.width * (end - start)).coerceAtLeast(size.height)
        drawRoundRect(
            brush = Brush.horizontalGradient(listOf(cold, warm), startX = left, endX = left + width),
            topLeft = Offset(left, 0f),
            size = Size(width, size.height),
            cornerRadius = radius
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(text = text, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline)
        )
    }
}

/** 06:00–18:59 arası gündüz sayılır; gece gökyüzü lacivert boyanır. */
private fun isNight(time: LocalDateTime) = time.hour < 6 || time.hour >= 19

/** [value]'nun [min]–[max] aralığındaki konumu, 0..1. */
private fun fraction(value: Double, min: Double, max: Double): Float =
    if (max <= min) 0.5f else ((value - min) / (max - min)).toFloat().coerceIn(0f, 1f)

private fun degrees(celsius: Double) = "${celsius.roundToInt()}°"

private fun percent(value: Int?) = value?.takeIf { it > 0 }?.let { "%$it" }

private const val HOURLY_COUNT = 24
private val TURKISH: Locale = Locale.forLanguageTag("tr")
private val HOUR_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val DAY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE", TURKISH)

private fun previewForecast(code: Int, hour: Int) = Forecast(
    current = CurrentWeather(
        temperatureC = 21.0,
        weatherCode = code,
        observedAt = LocalDateTime.of(2026, 10, 26, hour, 0),
        apparentTemperatureC = 20.0,
        humidityPercent = 45,
        windSpeedKmh = 12.0
    ),
    hourly = List(12) {
        HourlyForecast(
            time = LocalDateTime.now().plusHours(it.toLong()),
            temperatureC = 14.0 + (it % 6) * 1.5,
            weatherCode = if (it < 4) code else 3,
            precipitationProbability = it * 5
        )
    },
    daily = List(7) {
        DailyForecast(
            date = LocalDate.of(2026, 10, 26).plusDays(it.toLong()),
            minTemperatureC = 8.0 + it,
            maxTemperatureC = 17.0 + (it % 3) * 2,
            weatherCode = listOf(0, 2, 61, 3, 80, 0, 1)[it],
            precipitationProbability = listOf(0, 10, 70, 20, 55, 0, 5)[it]
        )
    }
)

@Preview(showBackground = true, heightDp = 1100)
@Composable
private fun ForecastScreenPreview() {
    HavaTheme {
        ForecastScreen(
            cityName = "Ankara",
            subtitle = "Ankara, Türkiye",
            uiState = LoadState.Success(previewForecast(code = 0, hour = 14)),
            onBack = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 1100)
@Composable
private fun ForecastScreenNightRainPreview() {
    HavaTheme(darkTheme = true) {
        ForecastScreen(
            cityName = "Rize",
            subtitle = "Rize, Türkiye",
            uiState = LoadState.Success(previewForecast(code = 61, hour = 22)),
            onBack = {},
            onRetry = {}
        )
    }
}
