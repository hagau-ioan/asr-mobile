package com.asr.financial.utils

import com.asr.financial.platform.Clock
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class DateUtilsTest {

    private class FakeClock(private val instant: Instant) : Clock {
        override fun now(): Instant = instant
        override fun formatDate(instant: Instant, pattern: String): String = ""
        override fun formatDateTime(instant: Instant, pattern: String): String = ""
        override fun parseDate(dateString: String, pattern: String): Instant? = null
    }

    @Test
    fun `getCurrentYear returns correct year from clock`() {
        // 2025-06-15T10:30:00Z
        val fakeClock = FakeClock(Instant.parse("2025-06-15T10:30:00Z"))
        
        val year = getCurrentYear(fakeClock)
        
        assertEquals(2025, year)
    }

    @Test
    fun `getCurrentMonth returns correct month from clock`() {
        // 2025-06-15T10:30:00Z (June = 6)
        val fakeClock = FakeClock(Instant.parse("2025-06-15T10:30:00Z"))
        
        val month = getCurrentMonth(fakeClock)
        
        assertEquals(6, month)
    }

    @Test
    fun `getAvailableYears returns range from 2024 to current year`() {
        // 2026-12-31T23:59:59Z
        val fakeClock = FakeClock(Instant.parse("2026-12-31T23:59:59Z"))
        
        val years = getAvailableYears(fakeClock)
        
        assertEquals(listOf(2024, 2025, 2026), years)
    }

    @Test
    fun `getAvailableYears returns only 2024 when current year is 2024`() {
        // 2024-01-01T00:00:00Z
        val fakeClock = FakeClock(Instant.parse("2024-01-01T00:00:00Z"))
        
        val years = getAvailableYears(fakeClock)
        
        assertEquals(listOf(2024), years)
    }
}
