package com.nurdan.hava.model

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * "Yakında yağmur var" uyarısı.
 *
 * @param hoursFromNow 0 ise yağmur şu anki saatte bekleniyor.
 * @param probability O saatin yağış olasılığı (%).
 */
data class RainAlert(val hoursFromNow: Int, val probability: Int) {

    companion object {
        /** Kaç saat ileriye bakılacağı. */
        const val WINDOW_HOURS = 3

        /** Bu olasılığın altındaki yağışlar için uyarı gösterilmez. */
        const val THRESHOLD_PERCENT = 50

        /**
         * Önümüzdeki [WINDOW_HOURS] saat içinde olasılığı [THRESHOLD_PERCENT] ve
         * üzerinde olan ilk saati bulur. Yoksa `null`: uyarı bandı hiç çizilmez.
         */
        fun find(hourly: List<HourlyForecast>, now: LocalDateTime): RainAlert? {
            val start = now.truncatedTo(ChronoUnit.HOURS)
            val end = start.plusHours(WINDOW_HOURS.toLong())
            return hourly
                .asSequence()
                .filter { !it.time.isBefore(start) && it.time.isBefore(end) }
                .sortedBy { it.time }
                .firstOrNull { (it.precipitationProbability ?: 0) >= THRESHOLD_PERCENT }
                ?.let { RainAlert(ChronoUnit.HOURS.between(start, it.time).toInt(), it.precipitationProbability ?: 0) }
        }
    }
}
