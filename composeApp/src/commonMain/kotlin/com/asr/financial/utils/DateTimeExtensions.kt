package com.asr.financial.utils

/**
 * Date and time utility functions
 */

/**
 * Calculate the start year for a 12-month period ending at given month/year
 * 
 * @param endMonth Month (1-12) where period ends
 * @param endYear Year where period ends
 * @return Start year of the 12-month period
 * 
 * Example: endMonth=12, endYear=2025 → returns 2025 (Dec 2025 - 12 months = Jan 2025)
 * Example: endMonth=6, endYear=2025 → returns 2024 (Jun 2025 - 12 months = Jul 2024)
 */
fun calculateStartYearFor12Months(endMonth: Int, endYear: Int): Int {
    return if (endMonth == 12) endYear else endYear - 1
}

/**
 * Calculate the start month for a 12-month period ending at given month
 * 
 * @param endMonth Month (1-12) where period ends
 * @return Start month (1-12) of the 12-month period
 * 
 * Example: endMonth=12 → returns 1 (January)
 * Example: endMonth=6 → returns 7 (July)
 */
fun calculateStartMonthFor12Months(endMonth: Int): Int {
    return if (endMonth == 12) 1 else endMonth + 1
}

/**
 * Calculate previous month and year
 * 
 * @param currentMonth Current month (1-12)
 * @param currentYear Current year
 * @return Pair of (previousMonth, previousYear)
 * 
 * Example: (1, 2026) → (12, 2025)
 * Example: (6, 2026) → (5, 2026)
 */
fun calculatePreviousMonth(currentMonth: Int, currentYear: Int): Pair<Int, Int> {
    return if (currentMonth == 1) {
        Pair(12, currentYear - 1)
    } else {
        Pair(currentMonth - 1, currentYear)
    }
}

/**
 * Calculate next month and year
 * 
 * @param currentMonth Current month (1-12)
 * @param currentYear Current year
 * @return Pair of (nextMonth, nextYear)
 * 
 * Example: (12, 2025) → (1, 2026)
 * Example: (6, 2025) → (7, 2025)
 */
fun calculateNextMonth(currentMonth: Int, currentYear: Int): Pair<Int, Int> {
    return if (currentMonth == 12) {
        Pair(1, currentYear + 1)
    } else {
        Pair(currentMonth + 1, currentYear)
    }
}

/**
 * Check if a given month/year is within the last N months from a reference point
 * 
 * @param targetMonth Month to check (1-12)
 * @param targetYear Year to check
 * @param referenceMonth Reference month (1-12)
 * @param referenceYear Reference year
 * @param monthsBack Number of months to look back (inclusive)
 * @return true if target is within the period
 * 
 * Example: target=(1,2025), reference=(12,2025), monthsBack=12 → true
 */
fun isWithinLastNMonths(
    targetMonth: Int,
    targetYear: Int,
    referenceMonth: Int,
    referenceYear: Int,
    monthsBack: Int
): Boolean {
    val targetMonthsFromStart = targetYear * 12 + targetMonth
    val referenceMonthsFromStart = referenceYear * 12 + referenceMonth
    val monthsDiff = referenceMonthsFromStart - targetMonthsFromStart
    
    return monthsDiff in 0 until monthsBack
}

/**
 * Get month name index (0-11) from month number (1-12)
 * Useful for array indexing
 */
fun monthNumberToIndex(month: Int): Int = month - 1

/**
 * Get month number (1-12) from month index (0-11)
 * Useful when working with arrays
 */
fun monthIndexToNumber(index: Int): Int = index + 1

/**
 * Parse date string components safely (year, month, day).
 * Handles date strings in format "YYYY-MM-DD".
 *
 * @param dateString Date string in "YYYY-MM-DD" format
 * @return Triple of (year, month, day) or null if parsing fails
 *
 * Example: "2025-06-15" -> Triple(2025, 6, 15)
 * Example: "invalid" -> null
 */
fun parseDateComponentsSafe(dateString: String): Triple<Int, Int, Int>? {
    return try {
        val parts = dateString.split("-")
        if (parts.size >= 3) {
            Triple(
                parts[0].toInt(),
                parts[1].toInt(),
                parts[2].toInt()
            )
        } else {
            null
        }
    } catch (e: NumberFormatException) {
        null
    }
}
