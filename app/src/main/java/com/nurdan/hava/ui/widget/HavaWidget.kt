package com.nurdan.hava.ui.widget

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.nurdan.hava.AppGraph
import com.nurdan.hava.MainActivity
import com.nurdan.hava.R
import com.nurdan.hava.data.OpenMeteoWeatherRepository
import com.nurdan.hava.model.City
import com.nurdan.hava.model.Forecast
import com.nurdan.hava.model.WeatherCondition
import kotlin.math.roundToInt

/**
 * Ana ekran widget'ı: ilk favori şehrin (yoksa en son bakılan şehrin) anlık havası.
 *
 * Glance, widget'ları Compose'a benzer bir dille yazmayı sağlar; ama Glance
 * bileşenleri ayrı bir pakettir (`androidx.glance.*`). Dokununca uygulama açılır.
 */
class HavaWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        AppGraph.init(context)
        val city = AppGraph.savedCities.widgetCity()
        val forecast = city?.let { fetch(it) }
        provideContent {
            WidgetContent(city = city, forecast = forecast, emptyText = context.getString(R.string.widget_empty))
        }
    }

    /** Ağ hatası widget'ı çökertmez; yalnızca şehir adı gösterilir. */
    private suspend fun fetch(city: City): Forecast? = try {
        OpenMeteoWeatherRepository().getForecast(city.latitude, city.longitude)
    } catch (e: Exception) {
        Log.w(TAG, "Widget tahmini alınamadı", e)
        null
    }

    private companion object {
        const val TAG = "HavaWidget"
    }
}

class HavaWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HavaWidget()
}

private val Coral = ColorProvider(Color(0xFFE4572E))
private val Paper = ColorProvider(Color(0xFFFBF6EE))
private val PaperSoft = ColorProvider(Color(0xCCFBF6EE))

@Composable
private fun WidgetContent(city: City?, forecast: Forecast?, emptyText: String) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Coral)
            .cornerRadius(24.dp)
            .padding(16.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        if (city == null) {
            Text("☁️", style = TextStyle(fontSize = 32.sp))
            Text(emptyText, style = TextStyle(color = Paper, fontSize = 14.sp, fontWeight = FontWeight.Medium))
            return@Column
        }
        Text(city.name, style = TextStyle(color = Paper, fontSize = 16.sp, fontWeight = FontWeight.Bold), maxLines = 1)
        if (forecast == null) {
            Text("—", style = TextStyle(color = Paper, fontSize = 40.sp))
            return@Column
        }
        val condition = WeatherCondition.fromWeatherCode(forecast.current.weatherCode)
        Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
            Text(
                "${forecast.current.temperatureC.roundToInt()}°",
                style = TextStyle(color = Paper, fontSize = 44.sp, fontWeight = FontWeight.Bold)
            )
            Spacer(GlanceModifier.padding(start = 8.dp))
            Text(condition.emoji, style = TextStyle(fontSize = 32.sp))
        }
        Text(condition.label, style = TextStyle(color = Paper, fontSize = 14.sp))
        forecast.daily.firstOrNull()?.let {
            Spacer(GlanceModifier.height(2.dp))
            Text(
                "↑ ${it.maxTemperatureC.roundToInt()}°   ↓ ${it.minTemperatureC.roundToInt()}°",
                style = TextStyle(color = PaperSoft, fontSize = 12.sp)
            )
        }
    }
}
