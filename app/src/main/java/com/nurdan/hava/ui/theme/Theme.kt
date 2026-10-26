package com.nurdan.hava.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/** Gündüz: krem kâğıt zemin, mürekkep yazı, mercan vurgu. */
private val DayColors = lightColorScheme(
    primary = Coral,
    onPrimary = Paper,
    primaryContainer = CoralMist,
    onPrimaryContainer = CoralDeep,
    secondary = Indigo,
    onSecondary = Paper,
    secondaryContainer = IndigoMist,
    onSecondaryContainer = Indigo,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = PaperDeep,
    onSurfaceVariant = InkSoft,
    surfaceContainer = PaperDeep,
    outline = PaperLine,
    outlineVariant = PaperLine,
    error = Brick
)

/** Gece: lacivert gökyüzü, ay ışığı yazı, yumuşak mercan vurgu. */
private val NightColors = darkColorScheme(
    primary = CoralLight,
    onPrimary = CoralDeep,
    primaryContainer = CoralNight,
    onPrimaryContainer = CoralMist,
    secondary = IndigoLight,
    onSecondary = Night,
    secondaryContainer = NightLine,
    onSecondaryContainer = IndigoMist,
    background = Night,
    onBackground = Moonlight,
    surface = Night,
    onSurface = Moonlight,
    surfaceVariant = NightRaised,
    onSurfaceVariant = MoonlightSoft,
    surfaceContainer = NightSurface,
    outline = NightLine,
    outlineVariant = NightLine,
    error = Blush
)

/**
 * Uygulamanın teması. Varsayılan olarak telefonun sistem ayarını izler; koyu
 * modda otomatik olarak "gece" paletine geçer.
 */
@Composable
fun HavaTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) NightColors else DayColors,
        typography = HavaTypography,
        shapes = HavaShapes,
        content = content
    )
}
