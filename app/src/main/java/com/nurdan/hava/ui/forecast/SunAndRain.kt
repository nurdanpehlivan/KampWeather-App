package com.nurdan.hava.ui.forecast

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nurdan.hava.model.RainAlert
import com.nurdan.hava.model.SunPath
import com.nurdan.hava.ui.theme.HavaTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** "1 saat içinde yağmur bekleniyor · %70" bandı. Yalnızca gerçekten yağmur yaklaşıyorsa çizilir. */
@Composable
fun RainBanner(alert: RainAlert, modifier: Modifier = Modifier) {
    val text = when (alert.hoursFromNow) {
        0 -> "Şu an yağmur bekleniyor"
        else -> "${alert.hoursFromNow} saat içinde yağmur bekleniyor"
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("☔", style = MaterialTheme.typography.headlineSmall)
            Column(modifier = Modifier.weight(1f)) {
                Text(text, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Yağış olasılığı %${alert.probability}. Şemsiyeni yanına al.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * Gün doğumundan batımına yarım daire bir yay; güneş, günün ne kadarının geçtiğine
 * göre yay üzerinde ilerler. Geçilen kısım dolu, kalan kısım kesikli çizilir.
 */
@Composable
fun SunArcCard(sun: SunPath, modifier: Modifier = Modifier) {
    val track = MaterialTheme.colorScheme.outline
    val glow = MaterialTheme.colorScheme.primary
    val sunColor = Color(0xFFFFC93C)
    Surface(modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Güneş", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                Text(
                    text = "Gün ışığı ${sun.daylight.toHours()} sa ${sun.daylight.toMinutes() % 60} dk",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .padding(top = 12.dp)
            ) {
                val radius = minOf(size.width / 2f - 12.dp.toPx(), size.height - 8.dp.toPx())
                val center = Offset(size.width / 2f, size.height - 4.dp.toPx())
                val box = Size(radius * 2, radius * 2)
                val topLeft = Offset(center.x - radius, center.y - radius)
                val stroke = 3.dp.toPx()
                // Ufuk çizgisi
                drawLine(track, Offset(0f, center.y), Offset(size.width, center.y), strokeWidth = 1.dp.toPx())
                // Kalan yol: kesikli
                drawArc(
                    color = track,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = box,
                    style = Stroke(stroke, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
                )
                // Geçilen yol: dolu
                drawArc(
                    color = glow,
                    startAngle = 180f,
                    sweepAngle = 180f * sun.progress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = box,
                    style = Stroke(stroke, cap = StrokeCap.Round)
                )
                val angle = PI + PI * sun.progress
                val position = Offset(
                    center.x + (radius * cos(angle)).toFloat(),
                    center.y + (radius * sin(angle)).toFloat()
                )
                val color = if (sun.isUp) sunColor else track
                drawCircle(color.copy(alpha = 0.3f), radius = 14.dp.toPx(), center = position)
                drawCircle(color, radius = 8.dp.toPx(), center = position)
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                SunTime(label = "Doğuş", time = sun.sunrise, modifier = Modifier.weight(1f))
                SunTime(label = "Batış", time = sun.sunset, alignEnd = true, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SunTime(label: String, time: LocalDateTime, modifier: Modifier = Modifier, alignEnd: Boolean = false) {
    Column(modifier = modifier, horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Text(time.format(CLOCK), style = MaterialTheme.typography.titleLarge)
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private val CLOCK: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Preview(showBackground = true)
@Composable
private fun SunAndRainPreview() {
    HavaTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            RainBanner(RainAlert(hoursFromNow = 2, probability = 70))
            SunArcCard(
                SunPath.of(
                    now = LocalDateTime.of(2026, 10, 26, 14, 0),
                    sunrise = LocalDateTime.of(2026, 10, 26, 7, 12),
                    sunset = LocalDateTime.of(2026, 10, 26, 18, 4)
                )!!
            )
        }
    }
}
