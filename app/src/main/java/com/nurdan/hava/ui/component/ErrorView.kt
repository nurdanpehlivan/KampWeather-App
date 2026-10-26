package com.nurdan.hava.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nurdan.hava.R
import com.nurdan.hava.ui.theme.HavaTheme

/**
 * Hata görünümü. Kullanıcıya teknik ayrıntı değil anlaşılır bir cümle gösterilir
 * (ayrıntı Logcat'e gider) ve her zaman bir çıkış yolu sunulur: "Tekrar dene".
 *
 * @param onRetry null verilirse düğme çizilmez.
 */
@Composable
fun ErrorView(message: String, modifier: Modifier = Modifier, onRetry: (() -> Unit)? = null) {
    StatusIllustration(
        symbol = "🌩️",
        circleColor = MaterialTheme.colorScheme.primaryContainer,
        title = "Bir aksilik oldu",
        message = message,
        modifier = modifier,
        action = onRetry?.let { retry ->
            {
                Button(
                    onClick = retry,
                    shape = CircleShape,
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                    Text(text = stringResource(R.string.action_retry), modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun ErrorViewPreview() {
    HavaTheme {
        ErrorView(message = "İnternet bağlantını kontrol et.", onRetry = {})
    }
}
