package com.nurdan.hava.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.nurdan.hava.model.WeatherCondition
import java.time.LocalDateTime

/**
 * Havanın ve günün saatinin gökyüzü renkleri (üstten alta).
 * Açık havada mavi, gece lacivert, yağmurda gri-mavi, fırtınada mor-gri, karda buz mavisi.
 */
fun skyColors(condition: WeatherCondition, night: Boolean): List<Color> = when (condition) {
    WeatherCondition.CLEAR, WeatherCondition.MAINLY_CLEAR ->
        if (night) {
            listOf(
                Color(0xFF0B1D3A),
                Color(0xFF1E3D6B),
                Color(0xFF34588C)
            )
        } else {
            listOf(Color(0xFF2F80ED), Color(0xFF56A8F5), Color(0xFF8CCBFF))
        }
    WeatherCondition.PARTLY_CLOUDY ->
        if (night) {
            listOf(
                Color(0xFF131E33),
                Color(0xFF2A3A57),
                Color(0xFF45567A)
            )
        } else {
            listOf(Color(0xFF4A7FC1), Color(0xFF6E9BD3), Color(0xFFA7C3E6))
        }
    WeatherCondition.OVERCAST ->
        if (night) {
            listOf(
                Color(0xFF1A1F2B),
                Color(0xFF30384A),
                Color(0xFF4A5366)
            )
        } else {
            listOf(Color(0xFF5E6F88), Color(0xFF7D8DA6), Color(0xFFA6B2C4))
        }
    WeatherCondition.FOG ->
        if (night) {
            listOf(
                Color(0xFF242A35),
                Color(0xFF3D4452),
                Color(0xFF59606E)
            )
        } else {
            listOf(Color(0xFF7A8699), Color(0xFF98A3B4), Color(0xFFBCC4D0))
        }
    WeatherCondition.DRIZZLE, WeatherCondition.RAIN, WeatherCondition.RAIN_SHOWERS ->
        if (night) {
            listOf(
                Color(0xFF101A28),
                Color(0xFF233448),
                Color(0xFF39506A)
            )
        } else {
            listOf(Color(0xFF34495E), Color(0xFF52708E), Color(0xFF7B97B3))
        }
    WeatherCondition.SNOW, WeatherCondition.SNOW_SHOWERS ->
        if (night) {
            listOf(
                Color(0xFF1C2B40),
                Color(0xFF3A5272),
                Color(0xFF5C7899)
            )
        } else {
            listOf(Color(0xFF5B85B5), Color(0xFF86A9CF), Color(0xFFB9CFE6))
        }
    WeatherCondition.THUNDERSTORM -> listOf(Color(0xFF1B1730), Color(0xFF362E52), Color(0xFF574B78))
    WeatherCondition.UNKNOWN -> listOf(Color(0xFF2F80ED), Color(0xFF56A8F5), Color(0xFF8CCBFF))
}

fun skyBrush(condition: WeatherCondition, night: Boolean): Brush = Brush.verticalGradient(skyColors(condition, night))

/** 06:00–18:59 arası gündüz. Gün doğumu bilgisi yoksa bu kaba kural kullanılır. */
fun isNightAt(time: LocalDateTime): Boolean = time.hour < 6 || time.hour >= 19

/**
 * Tüm ekranı kaplayan gökyüzü zemini. Hava değişince renkler yumuşakça geçer
 * (örneğin bir şehirden ötekine geçerken).
 */
@Composable
fun Modifier.sky(condition: WeatherCondition, night: Boolean): Modifier {
    val target = skyColors(condition, night)
    val top by animateColorAsState(target[0], tween(700), label = "skyTop")
    val middle by animateColorAsState(target[1], tween(700), label = "skyMiddle")
    val bottom by animateColorAsState(target[2], tween(700), label = "skyBottom")
    return background(Brush.verticalGradient(listOf(top, middle, bottom)))
}

/** Gökyüzünün üstündeki yarı saydam cam kart. */
fun Modifier.glass(shape: Shape, strong: Boolean = false): Modifier =
    background(if (strong) GlassStrong else Glass, shape).border(1.dp, GlassLine, shape)
