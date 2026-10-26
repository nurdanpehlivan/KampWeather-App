package com.nurdan.hava.ui.forecast

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nurdan.hava.R
import com.nurdan.hava.model.CurrentWeather
import com.nurdan.hava.model.DailyForecast
import com.nurdan.hava.model.Forecast
import com.nurdan.hava.model.HourlyForecast
import com.nurdan.hava.model.MascotMood
import com.nurdan.hava.model.RainAlert
import com.nurdan.hava.model.SunPath
import com.nurdan.hava.model.WeatherCondition
import com.nurdan.hava.ui.LoadState
import com.nurdan.hava.ui.component.EmptyView
import com.nurdan.hava.ui.component.ErrorView
import com.nurdan.hava.ui.mascot.CloudMascot
import com.nurdan.hava.ui.theme.HavaTheme
import com.nurdan.hava.ui.theme.glass
import com.nurdan.hava.ui.theme.isNightAt
import com.nurdan.hava.ui.theme.sky
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
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()

    ForecastScreen(
        cityName = viewModel.cityName,
        subtitle = viewModel.subtitle,
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::onRetry,
        isFavorite = isFavorite,
        onToggleFavorite = viewModel::onToggleFavorite,
        modifier = modifier
    )
}

/**
 * Bir şehrin hava tahmini. Bütün ekran o şehrin o anki gökyüzüyle boyanır;
 * bilgiler bu gökyüzünün üstünde cam kutularda durur.
 */
@Composable
fun ForecastScreen(
    cityName: String,
    subtitle: String,
    uiState: LoadState<Forecast>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {}
) {
    val forecast = (uiState as? LoadState.Success)?.data
    val condition = forecast?.let { WeatherCondition.fromWeatherCode(it.current.weatherCode) } ?: WeatherCondition.UNKNOWN
    val night = forecast?.let { isNightAt(it.current.observedAt) } ?: isNightAt(LocalDateTime.now())
    val title = cityName.ifEmpty { stringResource(R.string.detail_title) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .sky(condition, night)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopRow(
                title = if (forecast == null) title else "",
                onBack = onBack,
                isFavorite = isFavorite,
                onToggleFavorite = onToggleFavorite
            )
            when (uiState) {
                is LoadState.Success -> ForecastContent(
                    cityName = title,
                    subtitle = subtitle,
                    forecast = uiState.data,
                    condition = condition,
                    night = night
                )
                else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    when (uiState) {
                        LoadState.Loading -> CircularProgressIndicator(color = Color.White, strokeCap = StrokeCap.Round)
                        LoadState.Empty -> EmptyView(title = stringResource(R.string.detail_empty))
                        is LoadState.Error -> ErrorView(message = uiState.message, onRetry = onRetry)
                        is LoadState.Success -> Unit
                    }
                }
            }
        }
    }
}

/** Geri düğmesi ve favori kalbi; ikisi de cam daireler içinde. */
@Composable
private fun TopRow(title: String, onBack: () -> Unit, isFavorite: Boolean, onToggleFavorite: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.action_back),
                tint = Color.White,
                modifier = Modifier
                    .glass(CircleShape)
                    .padding(8.dp)
                    .size(20.dp)
            )
        }
        Text(title, style = MaterialTheme.typography.titleLarge, color = Color.White, modifier = Modifier.weight(1f))
        val heartScale by animateFloatAsState(if (isFavorite) 1.12f else 1f, label = "heart")
        IconButton(onClick = onToggleFavorite) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = stringResource(if (isFavorite) R.string.action_favorite_remove else R.string.action_favorite_add),
                tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.White,
                modifier = Modifier
                    .glass(CircleShape)
                    .padding(8.dp)
                    .size(20.dp)
                    .scale(heartScale)
            )
        }
    }
}

@Composable
private fun ForecastContent(cityName: String, subtitle: String, forecast: Forecast, condition: WeatherCondition, night: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Hero(cityName, subtitle, forecast.current, forecast.daily.firstOrNull(), condition, night)
        RainAlert.find(forecast.hourly, forecast.current.observedAt)?.let {
            RainBanner(alert = it, modifier = Modifier.padding(horizontal = 20.dp))
        }
        GlassSection(title = stringResource(R.string.detail_hourly)) {
            HourlyColumns(hours = forecast.hourly)
        }
        MetricTiles(current = forecast.current, modifier = Modifier.padding(horizontal = 20.dp))
        GlassSection(title = stringResource(R.string.detail_daily)) {
            WeekRows(days = forecast.daily)
        }
        forecast.daily.firstOrNull()?.let { today ->
            SunPath.of(forecast.current.observedAt, today.sunrise, today.sunset)?.let {
                SunArcCard(sun = it, modifier = Modifier.padding(horizontal = 20.dp))
            }
        }
    }
}

/** Ortalanmış büyük blok: şehir, ince dev sıcaklık, durum, en yüksek/en düşük, maskot ve sözü. */
@Composable
private fun Hero(
    cityName: String,
    subtitle: String,
    current: CurrentWeather,
    today: DailyForecast?,
    condition: WeatherCondition,
    night: Boolean
) {
    val mood = MascotMood.from(condition, night)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(cityName, style = MaterialTheme.typography.headlineLarge, color = Color.White, textAlign = TextAlign.Center)
        if (subtitle.isNotEmpty()) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
        Text(
            text = degrees(current.temperatureC),
            style = MaterialTheme.typography.displayLarge,
            color = Color.White,
            modifier = Modifier.padding(start = 24.dp)
        )
        Text(condition.label, style = MaterialTheme.typography.titleLarge, color = Color.White)
        today?.let {
            Text(
                text = "En yüksek ${degrees(it.maxTemperatureC)}   En düşük ${degrees(it.minTemperatureC)}",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Row(
            modifier = Modifier.padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CloudMascot(mood = mood, size = 76.dp)
            Text(
                text = mood.line,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                modifier = Modifier
                    .glass(RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
    }
}

/** Başlıklı cam kutu. */
@Composable
private fun GlassSection(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .glass(RoundedCornerShape(24.dp))
            .padding(vertical = 14.dp)
    ) {
        Text(
            text = title.uppercase(TURKISH),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.75f),
            modifier = Modifier.padding(start = 16.dp, bottom = 10.dp)
        )
        content()
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
            Column(
                modifier = Modifier
                    .weight(1f)
                    .glass(RoundedCornerShape(22.dp))
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(symbol, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = label.uppercase(TURKISH),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(value, style = MaterialTheme.typography.titleLarge, color = Color.White, maxLines = 1, softWrap = false)
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
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        itemsIndexed(items = upcoming, key = { _, hour -> hour.time.toString() }) { index, hour ->
            val first = index == 0
            Column(
                modifier = Modifier
                    .width(56.dp)
                    .then(if (first) Modifier.glass(CircleShape, strong = true) else Modifier)
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (first) stringResource(R.string.now) else hour.time.format(HOUR_FORMAT),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
                Text(WeatherCondition.fromWeatherCode(hour.weatherCode).emoji, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = percent(hour.precipitationProbability) ?: " ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                TemperatureStem(fraction = fraction(hour.temperatureC, min, max))
                Text(degrees(hour.temperatureC), style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
        }
    }
}

/** Sabit yükseklikte bir kutu; içindeki çubuk alttan [fraction] oranında dolar. */
@Composable
private fun TemperatureStem(fraction: Float) {
    val sun = MaterialTheme.colorScheme.primary
    Canvas(modifier = Modifier.size(width = 5.dp, height = 40.dp)) {
        val radius = CornerRadius(size.width / 2)
        drawRoundRect(color = Color.White.copy(alpha = 0.2f), cornerRadius = radius)
        val filled = size.height * (0.2f + 0.8f * fraction)
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(sun, Color.White), startY = size.height - filled, endY = size.height),
            topLeft = Offset(0f, size.height - filled),
            size = Size(size.width, filled),
            cornerRadius = radius
        )
    }
}

/** Haftalık satırlar: her gün için haftanın en soğuğundan en sıcağına uzanan bir aralık çubuğu. */
@Composable
private fun WeekRows(days: List<DailyForecast>) {
    if (days.isEmpty()) return
    val weekMin = days.minOf { it.minTemperatureC }
    val weekMax = days.maxOf { it.maxTemperatureC }
    Column {
        days.forEachIndexed { index, day ->
            if (index > 0) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.15f))
                )
            }
            DayRow(day = day, isToday = index == 0, weekMin = weekMin, weekMax = weekMax)
        }
    }
}

@Composable
private fun DayRow(day: DailyForecast, isToday: Boolean, weekMin: Double, weekMax: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isToday) stringResource(R.string.today) else day.date.format(DAY_FORMAT).replaceFirstChar(Char::titlecase),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            modifier = Modifier.width(56.dp)
        )
        Column(modifier = Modifier.width(44.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(WeatherCondition.fromWeatherCode(day.weatherCode).emoji, style = MaterialTheme.typography.titleLarge)
            percent(day.precipitationProbability)?.let {
                Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            }
        }
        Text(
            text = degrees(day.minTemperatureC),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.End,
            modifier = Modifier.width(40.dp)
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
            color = Color.White,
            modifier = Modifier.width(36.dp)
        )
    }
}

/** Soluk bir ray üzerinde, soğuktan (buz mavisi) sıcağa (güneş sarısı) giden renkli bir parça. */
@Composable
private fun RangeBar(start: Float, end: Float, modifier: Modifier = Modifier) {
    val cold = MaterialTheme.colorScheme.secondary
    val warm = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier.height(5.dp)) {
        val radius = CornerRadius(size.height / 2)
        drawRoundRect(color = Color.White.copy(alpha = 0.2f), cornerRadius = radius)
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

/** [value]'nun [min]–[max] aralığındaki konumu, 0..1. */
private fun fraction(value: Double, min: Double, max: Double): Float =
    if (max <= min) 0.5f else ((value - min) / (max - min)).toFloat().coerceIn(0f, 1f)

private fun degrees(celsius: Double) = "${celsius.roundToInt()}°"

private fun percent(value: Int?) = value?.takeIf { it > 0 }?.let { "%$it" }

private const val HOURLY_COUNT = 24
private val TURKISH: Locale = Locale.forLanguageTag("tr")
private val HOUR_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val DAY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE", TURKISH)

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
            precipitationProbability = it * 12
        )
    },
    daily = List(7) {
        DailyForecast(
            date = LocalDate.of(2026, 10, 26).plusDays(it.toLong()),
            minTemperatureC = 8.0 + it,
            maxTemperatureC = 17.0 + (it % 3) * 2,
            weatherCode = listOf(0, 2, 61, 3, 80, 0, 1)[it],
            precipitationProbability = listOf(0, 10, 70, 20, 55, 0, 5)[it],
            sunrise = LocalDateTime.of(2026, 10, 26, 7, 12).plusDays(it.toLong()),
            sunset = LocalDateTime.of(2026, 10, 26, 18, 4).plusDays(it.toLong())
        )
    }
)

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun ForecastScreenPreview() {
    HavaTheme {
        ForecastScreen(
            cityName = "Ankara",
            subtitle = "Ankara, Türkiye",
            uiState = LoadState.Success(previewForecast(code = 0, hour = 14)),
            onBack = {},
            onRetry = {},
            isFavorite = true
        )
    }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun ForecastScreenNightRainPreview() {
    HavaTheme {
        ForecastScreen(
            cityName = "Rize",
            subtitle = "Rize, Türkiye",
            uiState = LoadState.Success(previewForecast(code = 61, hour = 22)),
            onBack = {},
            onRetry = {}
        )
    }
}
