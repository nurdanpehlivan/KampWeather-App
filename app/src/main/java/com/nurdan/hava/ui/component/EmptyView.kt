package com.nurdan.hava.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nurdan.hava.model.MascotMood
import com.nurdan.hava.ui.mascot.CloudMascot
import com.nurdan.hava.ui.theme.HavaTheme

/**
 * "Sonuç yok" görünümü: şaşkın bulut maskot, serif başlık ve yönlendirici bir cümle.
 * Kullanıcı uygulamanın bozulmadığını, yalnızca sonucun olmadığını anlar.
 */
@Composable
fun EmptyView(title: String, modifier: Modifier = Modifier, message: String? = null, mood: MascotMood = MascotMood.CONFUSED) {
    StatusIllustration(
        mood = mood,
        circleColor = MaterialTheme.colorScheme.secondaryContainer,
        title = title,
        message = message,
        modifier = modifier
    )
}

/** Boş ve hata görünümlerinin ortak iskeleti; alt kısma isteğe bağlı bir eylem eklenir. */
@Composable
internal fun StatusIllustration(
    mood: MascotMood,
    circleColor: Color,
    title: String,
    message: String?,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier.padding(horizontal = 40.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(modifier = Modifier.size(132.dp), shape = CircleShape, color = circleColor) {
            Box(contentAlignment = Alignment.Center) {
                CloudMascot(mood = mood, size = 96.dp)
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        message?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        action?.invoke()
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyViewPreview() {
    HavaTheme {
        EmptyView(
            title = "Şehir bulunamadı",
            message = "Harfleri kontrol et ya da yakındaki büyük bir şehri dene."
        )
    }
}
