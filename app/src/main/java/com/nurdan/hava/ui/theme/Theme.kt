package com.nurdan.hava.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Tek bir renk şeması: gündüz ya da gece ayrımını sistem ayarı değil, gökyüzünün
 * kendisi yapar. Material rolleri cam kartlara göre ayarlanmıştır:
 * `surfaceVariant` cam kart, `onSurface` beyaz yazı, `primary` güneş sarısı vurgu.
 */
private val SkyColors = darkColorScheme(
    primary = SunYellow,
    onPrimary = DeepSky,
    primaryContainer = GlassStrong,
    onPrimaryContainer = Cloud,
    secondary = RainBlue,
    onSecondary = DeepSky,
    secondaryContainer = GlassStrong,
    onSecondaryContainer = Cloud,
    background = NightFallback,
    onBackground = Cloud,
    surface = NightFallback,
    onSurface = Cloud,
    surfaceVariant = Glass,
    onSurfaceVariant = CloudSoft,
    surfaceContainer = Glass,
    outline = GlassLine,
    outlineVariant = GlassLine,
    error = Alert
)

/**
 * Uygulamanın teması. `darkTheme` parametresi önizlemeler için duruyor; şema
 * gökyüzüne göre çalıştığı için iki durumda da aynıdır.
 */
@Composable
fun HavaTheme(@Suppress("UNUSED_PARAMETER") darkTheme: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SkyColors,
        typography = HavaTypography,
        shapes = HavaShapes
    ) {
        // Gökyüzü zemininde Scaffold saydamdır; varsayılan yazı rengi siyaha düşmesin.
        CompositionLocalProvider(LocalContentColor provides Cloud, content = content)
    }
}
