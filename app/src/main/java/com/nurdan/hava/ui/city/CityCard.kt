package com.nurdan.hava.ui.city

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nurdan.hava.R
import com.nurdan.hava.model.City
import com.nurdan.hava.model.LicensePlate
import com.nurdan.hava.ui.theme.HavaTheme
import java.util.Locale
import kotlin.math.abs

/**
 * Listedeki tek bir şehir.
 *
 * Solda rozet (Türkiye'de plaka, diğer ülkelerde bayrak), ortada ad ve bölge,
 * sağda koordinat ve ok. Seçili şehir mercan kenarlık ve açık zeminle vurgulanır.
 */
@Composable
fun CityCard(city: City, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val container by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        label = "cityCardContainer"
    )
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = container,
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CityBadge(city)
            Column(modifier = Modifier.weight(1f)) {
                if (selected) {
                    Text(
                        text = stringResource(R.string.city_selected).uppercase(Locale.forLanguageTag("tr")),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = city.name,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = city.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.background) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.padding(6.dp).size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = coordinates(city),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Türkiye'deki şehirler için renkli plaka rozeti ("06"), diğerleri için bayrak.
 * Plaka renkleri numaradan türetilir; her ilin kendi tonu vardır.
 */
@Composable
private fun CityBadge(city: City, modifier: Modifier = Modifier) {
    val plate = LicensePlate.of(city)
    val background = plate?.let { plateColor(it.toInt()) } ?: MaterialTheme.colorScheme.background
    Surface(modifier = modifier.size(52.dp), shape = MaterialTheme.shapes.medium, color = background) {
        Box(contentAlignment = Alignment.Center) {
            if (plate != null) {
                Text(text = plate, style = MaterialTheme.typography.titleLarge, color = Color.White)
            } else {
                Text(text = flagEmoji(city.countryCode), style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}

/** Plaka numarasından, açık ve koyu temada okunur kalan doygun bir renk üretir. */
private fun plateColor(plate: Int): Color = Color.hsl(hue = (plate * 47f) % 360f, saturation = 0.55f, lightness = 0.45f)

/** "DE" → 🇩🇪. Ülke kodu yoksa dünya simgesi. */
private fun flagEmoji(countryCode: String?): String {
    val code = countryCode?.uppercase(Locale.ROOT)?.takeIf { it.length == 2 && it.all(Char::isLetter) } ?: return "🌍"
    return code.map { String(Character.toChars(0x1F1E6 + (it - 'A'))) }.joinToString("")
}

/** 39.92, 32.85 → "39.9°K · 32.9°D". */
private fun coordinates(city: City): String {
    val lat = "%.1f°%s".format(Locale.ROOT, abs(city.latitude), if (city.latitude >= 0) "K" else "G")
    val lon = "%.1f°%s".format(Locale.ROOT, abs(city.longitude), if (city.longitude >= 0) "D" else "B")
    return "$lat · $lon"
}

@Preview(showBackground = true)
@Composable
private fun CityCardPreview() {
    HavaTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CityCard(
                city = City(323786, "Ankara", 39.91987, 32.85427, "TR", "Türkiye", "Ankara"),
                selected = true,
                onClick = {}
            )
            CityCard(
                city = City(2950159, "Berlin", 52.52437, 13.41053, "DE", "Almanya", "Berlin"),
                selected = false,
                onClick = {}
            )
        }
    }
}
