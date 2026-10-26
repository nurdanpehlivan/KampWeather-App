package com.nurdan.hava.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.nurdan.hava.model.WeatherCondition

/**
 * Hava durumuna göre tahmin ekranının üst bölümünü boyayan gökyüzü gradyanı.
 * Açık havada gün batımı turuncusu, yağmurda gri-mavi, fırtınada mor-lacivert.
 */
fun skyBrush(condition: WeatherCondition, night: Boolean): Brush {
    val colors = when (condition) {
        WeatherCondition.CLEAR, WeatherCondition.MAINLY_CLEAR ->
            if (night) listOf(Color(0xFF1B1A3A), Color(0xFF3D3B8E)) else listOf(Color(0xFFFF9A5A), Color(0xFFE4572E))
        WeatherCondition.PARTLY_CLOUDY ->
            if (night) listOf(Color(0xFF22213A), Color(0xFF4B4970)) else listOf(Color(0xFFF7B267), Color(0xFF8E7DBE))
        WeatherCondition.OVERCAST, WeatherCondition.FOG ->
            if (night) listOf(Color(0xFF26252F), Color(0xFF4A4858)) else listOf(Color(0xFFB8B3C4), Color(0xFF7D7790))
        WeatherCondition.DRIZZLE, WeatherCondition.RAIN, WeatherCondition.RAIN_SHOWERS ->
            if (night) listOf(Color(0xFF15233A), Color(0xFF2E4A6B)) else listOf(Color(0xFF6C8EB5), Color(0xFF3E5C82))
        WeatherCondition.SNOW, WeatherCondition.SNOW_SHOWERS ->
            if (night) listOf(Color(0xFF243447), Color(0xFF5A7391)) else listOf(Color(0xFFDDE7F2), Color(0xFF9DB4CF))
        WeatherCondition.THUNDERSTORM -> listOf(Color(0xFF231B3D), Color(0xFF5B3B8C))
        WeatherCondition.UNKNOWN -> if (night) listOf(NightRaised, NightLine) else listOf(Coral, Indigo)
    }
    return Brush.verticalGradient(colors)
}

/** Gradyanın üstündeki yazı rengi: kar gibi açık zeminlerde koyu, diğerlerinde açık. */
fun onSky(condition: WeatherCondition, night: Boolean): Color =
    if (!night && (condition == WeatherCondition.SNOW || condition == WeatherCondition.SNOW_SHOWERS)) Ink else Paper
