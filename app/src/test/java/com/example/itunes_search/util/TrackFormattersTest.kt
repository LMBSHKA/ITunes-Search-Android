package com.example.itunes_search.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TrackFormattersTest {
    @Test
    fun durationUsesMinutesAndTwoDigitSeconds() {
        assertEquals("5:20", formatDuration(320357L))
        assertEquals("9:04", formatDuration(544629L))
        assertEquals("0:00", formatDuration(0L))
        assertEquals("—", formatDuration(null))
        assertEquals("—", formatDuration(-1L))
    }

    @Test
    fun releaseDateUsesTheCalendarDateFromTheApi() {
        assertEquals("30.11.2000", formatReleaseDate("2000-11-30T08:00:00Z"))
        assertEquals("—", formatReleaseDate(null))
        assertEquals("—", formatReleaseDate("invalid"))
        assertEquals("—", formatReleaseDate("2026-02-30T00:00:00Z"))
    }

    @Test
    fun priceUsesTheSpecifiedCurrencyAndHandlesMissingFields() {
        assertEquals("\$1.29", formatPrice(1.29, "USD"))
        assertEquals("1.29", formatPrice(1.29, null))
        assertEquals("1.29 INVALID", formatPrice(1.29, "INVALID"))
        assertEquals("—", formatPrice(null, "USD"))
        assertEquals("—", formatPrice(-1.0, "USD"))
        assertEquals("—", formatPrice(Double.NaN, "USD"))
    }
}
