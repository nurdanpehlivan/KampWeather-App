package com.nurdan.hava.ui.mascot

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nurdan.hava.model.MascotMood
import com.nurdan.hava.ui.theme.HavaTheme

private val CloudWhite = Color(0xFFFFFFFF)
private val CloudNight = Color(0xFFE6E3FF)
private val CloudStorm = Color(0xFFCFCBDA)
private val Face = Color(0xFF1E1B2E)
private val Blush = Color(0xFFFF8A80)
private val Accent = Color(0xFFE4572E)
private val Rain = Color(0xFF6FA8DC)
private val Bolt = Color(0xFFFFC93C)

/**
 * Uygulamanın maskotu: havaya göre kılık değiştiren minik bir bulut.
 *
 * Tamamen Canvas ile çizilir (resim dosyası yok). Yavaşça aşağı yukarı süzülür ve
 * arada bir göz kırpar. Çizim 100×100 birimlik bir ızgarada tarif edilir; [size]
 * ne olursa olsun oranlar korunur.
 */
@Composable
fun CloudMascot(mood: MascotMood, modifier: Modifier = Modifier, size: Dp = 120.dp) {
    val transition = rememberInfiniteTransition(label = "mascot")
    val bob by transition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "mascotBob"
    )
    val blink by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = 3600
                1f at 3300
                0.1f at 3420
                1f at 3540
            }
        ),
        label = "mascotBlink"
    )
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing)),
        label = "mascotDrift"
    )
    Canvas(modifier = modifier.size(size).semantics { contentDescription = mood.line }) {
        val u = this.size.minDimension / 100f
        when (mood) {
            MascotMood.RAINY -> drawUmbrella(u)
            MascotMood.NIGHT -> drawMoon(u)
            else -> Unit
        }
        translate(top = bob * u) {
            val body = when (mood) {
                MascotMood.NIGHT -> CloudNight
                MascotMood.STORMY, MascotMood.UPSET -> CloudStorm
                else -> CloudWhite
            }
            drawCloud(u, body)
            drawFace(u, mood, blink)
            if (mood == MascotMood.SNOWY) drawScarf(u)
        }
        when (mood) {
            MascotMood.RAINY -> drawFalling(u, drift, Rain, xs = listOf(12f, 88f, 20f, 80f), drop = true)
            MascotMood.SNOWY -> drawFalling(u, drift, Color.White, xs = listOf(10f, 90f, 24f, 76f, 50f), drop = false)
            MascotMood.STORMY -> drawBolt(u)
            MascotMood.FOGGY -> drawFog(u, drift)
            MascotMood.SUNNY -> drawRays(u, drift)
            else -> Unit
        }
    }
}

/** Maskot ve yanında konuşma balonu. */
@Composable
fun MascotWithLine(mood: MascotMood, modifier: Modifier = Modifier, size: Dp = 84.dp, bubble: @Composable () -> Unit) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        CloudMascot(mood = mood, size = size)
        bubble()
    }
}

private fun DrawScope.p(u: Float, x: Float, y: Float) = Offset(x * u, y * u)

private fun DrawScope.drawCloud(u: Float, color: Color) {
    // Yumuşak gölge
    drawOval(Color.Black.copy(alpha = 0.10f), topLeft = p(u, 22f, 86f), size = Size(56f * u, 6f * u))
    drawCircle(color, radius = 19f * u, center = p(u, 32f, 58f))
    drawCircle(color, radius = 25f * u, center = p(u, 52f, 47f))
    drawCircle(color, radius = 17f * u, center = p(u, 72f, 59f))
    drawRoundRect(color, topLeft = p(u, 14f, 56f), size = Size(74f * u, 26f * u), cornerRadius = CornerRadius(13f * u))
}

private fun DrawScope.drawFace(u: Float, mood: MascotMood, blink: Float) {
    val line = Stroke(width = 2.4f * u, cap = StrokeCap.Round)
    // Yanaklar
    if (mood != MascotMood.STORMY) {
        drawCircle(Blush.copy(alpha = 0.45f), radius = 4f * u, center = p(u, 34f, 70f))
        drawCircle(Blush.copy(alpha = 0.45f), radius = 4f * u, center = p(u, 66f, 70f))
    }
    // Gözler
    when (mood) {
        MascotMood.SUNNY -> {
            drawRoundRect(Face, topLeft = p(u, 33f, 57f), size = Size(14f * u, 9f * u), cornerRadius = CornerRadius(4f * u))
            drawRoundRect(Face, topLeft = p(u, 53f, 57f), size = Size(14f * u, 9f * u), cornerRadius = CornerRadius(4f * u))
            drawLine(Face, p(u, 47f, 60f), p(u, 53f, 60f), strokeWidth = 2f * u)
            drawLine(Color.White.copy(alpha = 0.7f), p(u, 36f, 59f), p(u, 39f, 59f), strokeWidth = 1.4f * u, cap = StrokeCap.Round)
            drawLine(Color.White.copy(alpha = 0.7f), p(u, 56f, 59f), p(u, 59f, 59f), strokeWidth = 1.4f * u, cap = StrokeCap.Round)
        }
        MascotMood.NIGHT, MascotMood.SNOWY -> {
            // Kapalı ya da gülen gözler: küçük yaylar
            val start = if (mood == MascotMood.NIGHT) 0f else 180f
            drawArc(Face, start, 180f, false, p(u, 36f, 58f), Size(9f * u, 7f * u), style = line)
            drawArc(Face, start, 180f, false, p(u, 55f, 58f), Size(9f * u, 7f * u), style = line)
        }
        MascotMood.FOGGY -> {
            drawLine(Face, p(u, 36f, 62f), p(u, 45f, 62f), strokeWidth = 2.4f * u, cap = StrokeCap.Round)
            drawLine(Face, p(u, 55f, 62f), p(u, 64f, 62f), strokeWidth = 2.4f * u, cap = StrokeCap.Round)
        }
        else -> {
            val eyeSize = Size(6.4f * u, 7.4f * u * blink)
            drawOval(Face, topLeft = Offset(37.8f * u, (61.5f * u) - eyeSize.height / 2), size = eyeSize)
            drawOval(Face, topLeft = Offset(55.8f * u, (61.5f * u) - eyeSize.height / 2), size = eyeSize)
            if (blink > 0.6f) {
                drawCircle(Color.White, radius = 1.2f * u, center = p(u, 42.2f, 59.6f))
                drawCircle(Color.White, radius = 1.2f * u, center = p(u, 60.2f, 59.6f))
            }
        }
    }
    // Kaşlar
    when (mood) {
        MascotMood.STORMY, MascotMood.UPSET -> {
            drawLine(Face, p(u, 35f, 52f), p(u, 44f, 55f), strokeWidth = 2f * u, cap = StrokeCap.Round)
            drawLine(Face, p(u, 65f, 52f), p(u, 56f, 55f), strokeWidth = 2f * u, cap = StrokeCap.Round)
        }
        MascotMood.CONFUSED -> drawLine(Face, p(u, 55f, 51f), p(u, 64f, 49f), strokeWidth = 2f * u, cap = StrokeCap.Round)
        else -> Unit
    }
    // Ağız
    when (mood) {
        MascotMood.SUNNY, MascotMood.SNOWY ->
            drawArc(Face, 0f, 180f, true, p(u, 43f, 67f), Size(14f * u, 10f * u))
        MascotMood.CLOUDY, MascotMood.RAINY, MascotMood.NIGHT ->
            drawArc(Face, 20f, 140f, false, p(u, 44f, 65f), Size(12f * u, 8f * u), style = line)
        MascotMood.FOGGY -> drawLine(Face, p(u, 46f, 72f), p(u, 54f, 72f), strokeWidth = 2.4f * u, cap = StrokeCap.Round)
        MascotMood.CONFUSED -> drawCircle(Face, radius = 2.6f * u, center = p(u, 50f, 72f), style = Stroke(2f * u))
        MascotMood.UPSET -> {
            drawArc(Face, 200f, 140f, false, p(u, 44f, 70f), Size(12f * u, 8f * u), style = line)
            drawCircle(Rain, radius = 2.2f * u, center = p(u, 40f, 70f))
        }
        MascotMood.STORMY -> {
            val zig = Path().apply {
                moveTo(42f * u, 72f * u)
                lineTo(46f * u, 69f * u)
                lineTo(50f * u, 72f * u)
                lineTo(54f * u, 69f * u)
                lineTo(58f * u, 72f * u)
            }
            drawPath(zig, Face, style = line)
        }
    }
}

private fun DrawScope.drawUmbrella(u: Float) {
    drawLine(Face, p(u, 50f, 26f), p(u, 50f, 44f), strokeWidth = 2.4f * u, cap = StrokeCap.Round)
    drawArc(Accent, 180f, 180f, true, p(u, 22f, 2f), Size(56f * u, 48f * u))
    // Kubbe dilimleri
    drawLine(Color.White.copy(alpha = 0.5f), p(u, 50f, 2f), p(u, 50f, 26f), strokeWidth = 1.6f * u)
    drawLine(Color.White.copy(alpha = 0.5f), p(u, 50f, 2f), p(u, 36f, 26f), strokeWidth = 1.6f * u)
    drawLine(Color.White.copy(alpha = 0.5f), p(u, 50f, 2f), p(u, 64f, 26f), strokeWidth = 1.6f * u)
    drawCircle(Accent, radius = 2f * u, center = p(u, 50f, 1.5f))
}

private fun DrawScope.drawScarf(u: Float) {
    drawRoundRect(Accent, topLeft = p(u, 24f, 76f), size = Size(52f * u, 7f * u), cornerRadius = CornerRadius(3.5f * u))
    drawRoundRect(Accent, topLeft = p(u, 60f, 79f), size = Size(8f * u, 15f * u), cornerRadius = CornerRadius(3f * u))
    drawLine(Color.White.copy(alpha = 0.6f), p(u, 62f, 90f), p(u, 66f, 90f), strokeWidth = 1.4f * u)
}

private fun DrawScope.drawMoon(u: Float) {
    drawCircle(Bolt, radius = 9f * u, center = p(u, 82f, 14f))
    drawCircle(CloudNight.copy(alpha = 0f), radius = 0f)
    drawCircle(Color(0xFF3D3B8E), radius = 8f * u, center = p(u, 86f, 11f))
    // "z z"
    val z = Stroke(width = 1.8f * u, cap = StrokeCap.Round)
    listOf(Offset(18f, 16f) to 7f, Offset(28f, 6f) to 5f).forEach { (o, s) ->
        val path = Path().apply {
            moveTo(o.x * u, o.y * u)
            lineTo((o.x + s) * u, o.y * u)
            lineTo(o.x * u, (o.y + s) * u)
            lineTo((o.x + s) * u, (o.y + s) * u)
        }
        drawPath(path, Face.copy(alpha = 0.55f), style = z)
    }
}

private fun DrawScope.drawFalling(u: Float, drift: Float, color: Color, xs: List<Float>, drop: Boolean) {
    xs.forEachIndexed { index, x ->
        val phase = (drift + index * 0.27f) % 1f
        val y = 40f + phase * 55f
        val alpha = 1f - phase
        if (drop) {
            drawLine(color.copy(alpha = alpha), p(u, x, y), p(u, x - 1.5f, y + 6f), strokeWidth = 2.2f * u, cap = StrokeCap.Round)
        } else {
            drawCircle(color.copy(alpha = alpha), radius = 2.2f * u, center = p(u, x, y))
            drawCircle(Rain.copy(alpha = alpha * 0.5f), radius = 2.2f * u, center = p(u, x, y), style = Stroke(0.8f * u))
        }
    }
}

private fun DrawScope.drawBolt(u: Float) {
    val bolt = Path().apply {
        moveTo(54f * u, 82f * u)
        lineTo(44f * u, 94f * u)
        lineTo(51f * u, 94f * u)
        lineTo(46f * u, 100f * u)
        lineTo(60f * u, 88f * u)
        lineTo(53f * u, 88f * u)
        close()
    }
    drawPath(bolt, Bolt)
}

private fun DrawScope.drawFog(u: Float, drift: Float) {
    val shift = (drift - 0.5f) * 6f
    listOf(80f, 88f, 95f).forEachIndexed { i, y ->
        val sx = if (i % 2 == 0) shift else -shift
        drawLine(Color.White.copy(alpha = 0.85f), p(u, 14f + sx, y), p(u, 86f + sx, y), strokeWidth = 3f * u, cap = StrokeCap.Round)
    }
}

private fun DrawScope.drawRays(u: Float, drift: Float) {
    val center = p(u, 84f, 18f)
    drawCircle(Bolt, radius = 8f * u, center = center)
    val length = 4f + drift * 2f
    for (i in 0 until 8) {
        val angle = Math.toRadians(i * 45.0)
        val start = Offset(center.x + (11f * u * kotlin.math.cos(angle)).toFloat(), center.y + (11f * u * kotlin.math.sin(angle)).toFloat())
        val end = Offset(
            center.x + ((11f + length) * u * kotlin.math.cos(angle)).toFloat(),
            center.y + ((11f + length) * u * kotlin.math.sin(angle)).toFloat()
        )
        drawLine(Bolt, start, end, strokeWidth = 2f * u, cap = StrokeCap.Round)
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun CloudMascotPreview() {
    HavaTheme {
        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(8.dp)) {
            MascotMood.entries.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { CloudMascot(mood = it, size = 110.dp) }
                }
            }
        }
        MaterialTheme.colorScheme
    }
}
