package com.nurdan.hava.model

import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RainAlertTest {

    private val now = LocalDateTime.parse("2026-10-26T14:20")

    private fun hour(time: String, probability: Int?) =
        HourlyForecast(time = LocalDateTime.parse(time), temperatureC = 15.0, weatherCode = 61, precipitationProbability = probability)

    @Test
    fun `esigi gecen ilk saat bulunur`() {
        val hours = listOf(
            hour("2026-10-26T14:00", 10),
            hour("2026-10-26T15:00", 40),
            hour("2026-10-26T16:00", 70),
            hour("2026-10-26T17:00", 90)
        )

        assertEquals(RainAlert(hoursFromNow = 2, probability = 70), RainAlert.find(hours, now))
    }

    @Test
    fun `su anki saat de sayilir`() {
        assertEquals(RainAlert(0, 55), RainAlert.find(listOf(hour("2026-10-26T14:00", 55)), now))
    }

    @Test
    fun `pencere disindaki yagmur uyari uretmez`() {
        val hours = listOf(hour("2026-10-26T17:00", 95), hour("2026-10-26T13:00", 95))

        assertNull(RainAlert.find(hours, now))
    }

    @Test
    fun `olasilik yoksa uyari yoktur`() {
        assertNull(RainAlert.find(listOf(hour("2026-10-26T15:00", null)), now))
    }
}
