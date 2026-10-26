package com.nurdan.hava.model

import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SunPathTest {

    private val sunrise = LocalDateTime.parse("2026-10-26T07:00")
    private val sunset = LocalDateTime.parse("2026-10-26T19:00")

    @Test
    fun `ogle vakti yolun yarisindadir`() {
        val sun = SunPath.of(LocalDateTime.parse("2026-10-26T13:00"), sunrise, sunset)!!

        assertEquals(0.5f, sun.progress, 0.001f)
        assertTrue(sun.isUp)
        assertEquals(12, sun.daylight.toHours())
    }

    @Test
    fun `gece ilerleme sinirlarda kalir`() {
        val early = SunPath.of(LocalDateTime.parse("2026-10-26T04:00"), sunrise, sunset)!!
        val late = SunPath.of(LocalDateTime.parse("2026-10-26T22:00"), sunrise, sunset)!!

        assertEquals(0f, early.progress, 0.001f)
        assertEquals(1f, late.progress, 0.001f)
        assertFalse(early.isUp)
        assertFalse(late.isUp)
    }

    @Test
    fun `eksik ya da bozuk veri null doner`() {
        val noon = LocalDateTime.parse("2026-10-26T12:00")

        assertNull(SunPath.of(noon, null, sunset))
        assertNull(SunPath.of(noon, sunset, sunrise))
    }
}
