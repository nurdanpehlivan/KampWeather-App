package com.nurdan.hava.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LicensePlateTest {

    private fun city(name: String, admin1: String?, countryCode: String = "TR") =
        City(id = 1L, name = name, latitude = 0.0, longitude = 0.0, countryCode = countryCode, country = null, admin1 = admin1)

    @Test
    fun `il adindan plaka bulunur`() {
        assertEquals("06", LicensePlate.of(city("Ankara", "Ankara")))
        assertEquals("34", LicensePlate.of(city("Kadıköy", "İstanbul")))
        assertEquals("81", LicensePlate.of(city("Düzce", "Düzce")))
    }

    @Test
    fun `tek haneli plakalar sifirla baslar`() {
        assertEquals("01", LicensePlate.of(city("Adana", "Adana")))
    }

    @Test
    fun `eski il adlari da taninir`() {
        assertEquals("33", LicensePlate.of(city("Tarsus", "İçel")))
    }

    @Test
    fun `turkiye disinda plaka yoktur`() {
        assertNull(LicensePlate.of(city("Berlin", "Berlin", countryCode = "DE")))
    }

    @Test
    fun `taninmayan ilde plaka yoktur`() {
        assertNull(LicensePlate.of(city("Bilinmeyen", "Bilinmeyen")))
    }
}
