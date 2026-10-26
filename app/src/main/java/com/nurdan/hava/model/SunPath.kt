package com.nurdan.hava.model

import java.time.Duration
import java.time.LocalDateTime

/**
 * Güneşin gökyüzündeki yolu: doğuştan batışa ne kadarının geçtiği.
 *
 * @param progress 0 = gün doğumu, 1 = gün batımı. Gece ise 0 ya da 1'e sabitlenir.
 * @param isUp Güneş şu an ufkun üstünde mi.
 */
data class SunPath(
    val sunrise: LocalDateTime,
    val sunset: LocalDateTime,
    val progress: Float,
    val isUp: Boolean
) {
    /** Gün ışığı süresi, örneğin 12 sa 9 dk. */
    val daylight: Duration get() = Duration.between(sunrise, sunset)

    companion object {
        /** Gün doğumu/batımı bilinmiyorsa ya da bozuksa `null`. */
        fun of(now: LocalDateTime, sunrise: LocalDateTime?, sunset: LocalDateTime?): SunPath? {
            if (sunrise == null || sunset == null || !sunset.isAfter(sunrise)) return null
            val total = Duration.between(sunrise, sunset).toMinutes().toFloat()
            val passed = Duration.between(sunrise, now).toMinutes().toFloat()
            val progress = (passed / total).coerceIn(0f, 1f)
            val isUp = !now.isBefore(sunrise) && now.isBefore(sunset)
            return SunPath(sunrise, sunset, progress, isUp)
        }
    }
}
