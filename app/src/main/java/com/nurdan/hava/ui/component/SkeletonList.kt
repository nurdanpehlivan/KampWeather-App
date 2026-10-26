package com.nurdan.hava.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nurdan.hava.ui.theme.HavaTheme

/**
 * Yükleniyor durumu: gerçek şehir kartlarıyla aynı ölçüde iskeletler.
 *
 * Kayan ışıltı yerine "nefes alan" bir animasyon kullanıyoruz: kartların saydamlığı
 * yavaşça azalıp artar ve her satır bir öncekinden biraz geç başlar; liste yukarıdan
 * aşağıya dalgalanır.
 */
@Composable
fun SkeletonList(modifier: Modifier = Modifier, itemCount: Int = 6) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 6.dp)
                .size(width = 120.dp, height = 10.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
        )
        repeat(itemCount) { index ->
            val alpha by transition.animateFloat(
                initialValue = 1f,
                targetValue = 0.35f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 900, delayMillis = index * 90, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "skeletonAlpha$index"
            )
            SkeletonCard(modifier = Modifier.alpha(alpha))
        }
    }
}

@Composable
private fun SkeletonCard(modifier: Modifier = Modifier) {
    val block = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.large)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(block, MaterialTheme.shapes.medium)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Bar(widthFraction = 0.6f, height = 18, color = block)
            Bar(widthFraction = 0.85f, height = 12, color = block)
        }
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(block, CircleShape)
        )
    }
}

@Composable
private fun Bar(widthFraction: Float, height: Int, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(height.dp)
            .background(color, RoundedCornerShape(6.dp))
    )
}

@Preview(showBackground = true)
@Composable
private fun SkeletonListPreview() {
    HavaTheme {
        SkeletonList()
    }
}
