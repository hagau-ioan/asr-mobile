package com.asr.financial.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DateTimeExtensionsTest {

    @Test
    fun `calculateStartYearFor12Months when ending in December returns same year`() {
        val result = calculateStartYearFor12Months(endMonth = 12, endYear = 2025)
        assertEquals(2025, result)
    }

    @Test
    fun `calculateStartYearFor12Months when ending in other month returns previous year`() {
        val result = calculateStartYearFor12Months(endMonth = 6, endYear = 2025)
        assertEquals(2024, result)
    }

    @Test
    fun `calculateStartMonthFor12Months when ending in December returns January`() {
        val result = calculateStartMonthFor12Months(endMonth = 12)
        assertEquals(1, result)
    }

    @Test
    fun `calculateStartMonthFor12Months when ending in June returns July`() {
        val result = calculateStartMonthFor12Months(endMonth = 6)
        assertEquals(7, result)
    }

    @Test
    fun `calculatePreviousMonth from January returns December of previous year`() {
        val (month, year) = calculatePreviousMonth(currentMonth = 1, currentYear = 2026)
        assertEquals(12, month)
        assertEquals(2025, year)
    }

    @Test
    fun `calculatePreviousMonth from June returns May of same year`() {
        val (month, year) = calculatePreviousMonth(currentMonth = 6, currentYear = 2026)
        assertEquals(5, month)
        assertEquals(2026, year)
    }

    @Test
    fun `calculateNextMonth from December returns January of next year`() {
        val (month, year) = calculateNextMonth(currentMonth = 12, currentYear = 2025)
        assertEquals(1, month)
        assertEquals(2026, year)
    }

    @Test
    fun `calculateNextMonth from June returns July of same year`() {
        val (month, year) = calculateNextMonth(currentMonth = 6, currentYear = 2025)
        assertEquals(7, month)
        assertEquals(2025, year)
    }

    @Test
    fun `isWithinLastNMonths returns true for current month`() {
        val result = isWithinLastNMonths(
            targetMonth = 12,
            targetYear = 2025,
            referenceMonth = 12,
            referenceYear = 2025,
            monthsBack = 12
        )
        assertTrue(result)
    }

    @Test
    fun `isWithinLastNMonths returns true for 11 months ago`() {
        val result = isWithinLastNMonths(
            targetMonth = 1,
            targetYear = 2025,
            referenceMonth = 12,
            referenceYear = 2025,
            monthsBack = 12
        )
        assertTrue(result)
    }

    @Test
    fun `isWithinLastNMonths returns false for 12 months ago`() {
        val result = isWithinLastNMonths(
            targetMonth = 12,
            targetYear = 2024,
            referenceMonth = 12,
            referenceYear = 2025,
            monthsBack = 12
        )
        assertFalse(result)
    }

    @Test
    fun `monthNumberToIndex converts correctly`() {
        assertEquals(0, monthNumberToIndex(1))  // January
        assertEquals(11, monthNumberToIndex(12)) // December
    }

    @Test
    fun `monthIndexToNumber converts correctly`() {
        assertEquals(1, monthIndexToNumber(0))  // January
        assertEquals(12, monthIndexToNumber(11)) // December
    }
}
