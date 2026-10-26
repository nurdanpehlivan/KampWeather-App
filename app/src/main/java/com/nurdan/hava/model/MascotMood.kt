package com.nurdan.hava.model

/**
 * Bulut maskotun havaya göre kılığı ve söylediği kısa cümle.
 */
enum class MascotMood(val line: String) {
    SUNNY("Güneş gözlüklerimi taktım, sen de tak!"),
    CLOUDY("Bugün biraz gölgeliyim ama keyfim yerinde."),
    FOGGY("Önümü göremiyorum… sen dikkatli sür."),
    RAINY("Şemsiyeni almadan çıkma!"),
    SNOWY("Atkımı sardım, kartopu var mı?"),
    STORMY("Gök gürlüyor! Bugün evde kalalım mı?"),
    NIGHT("İyi geceler, yıldızları say."),
    CONFUSED("Hmm… böyle bir yer bulamadım."),
    UPSET("Bağlantı koptu, bir daha deneyelim mi?");

    companion object {
        /** Hava durumunu kılığa çevirir. Gece açık havada uykucu bulut görünür. */
        fun from(condition: WeatherCondition, night: Boolean): MascotMood = when (condition) {
            WeatherCondition.CLEAR, WeatherCondition.MAINLY_CLEAR -> if (night) NIGHT else SUNNY
            WeatherCondition.PARTLY_CLOUDY, WeatherCondition.OVERCAST -> if (night) NIGHT else CLOUDY
            WeatherCondition.FOG -> FOGGY
            WeatherCondition.DRIZZLE, WeatherCondition.RAIN, WeatherCondition.RAIN_SHOWERS -> RAINY
            WeatherCondition.SNOW, WeatherCondition.SNOW_SHOWERS -> SNOWY
            WeatherCondition.THUNDERSTORM -> STORMY
            WeatherCondition.UNKNOWN -> CLOUDY
        }
    }
}
