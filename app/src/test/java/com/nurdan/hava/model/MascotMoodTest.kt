package com.nurdan.hava.model

import org.junit.Assert.assertEquals
import org.junit.Test

class MascotMoodTest {

    @Test
    fun `hava durumu dogru kiliga cevrilir`() {
        assertEquals(MascotMood.SUNNY, MascotMood.from(WeatherCondition.CLEAR, night = false))
        assertEquals(MascotMood.RAINY, MascotMood.from(WeatherCondition.RAIN_SHOWERS, night = false))
        assertEquals(MascotMood.SNOWY, MascotMood.from(WeatherCondition.SNOW, night = true))
        assertEquals(MascotMood.STORMY, MascotMood.from(WeatherCondition.THUNDERSTORM, night = false))
        assertEquals(MascotMood.FOGGY, MascotMood.from(WeatherCondition.FOG, night = false))
    }

    @Test
    fun `gece acik havada uykucu bulut gorunur`() {
        assertEquals(MascotMood.NIGHT, MascotMood.from(WeatherCondition.CLEAR, night = true))
        assertEquals(MascotMood.NIGHT, MascotMood.from(WeatherCondition.OVERCAST, night = true))
    }

    @Test
    fun `her kiligin bir cumlesi vardir`() {
        MascotMood.entries.forEach { assert(it.line.isNotBlank()) }
    }
}
