package com.asr.financial.utils

import com.asr.financial.platform.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

/**
 * Get current year
 */
fun getCurrentYear(clock: Clock): Int {
    return clock.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .year
}

/**
 * Get current month (1-12)
 */
fun getCurrentMonth(clock: Clock): Int {
    return clock.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .month.number
}

/**
 * Generate available years from 2024 to current year
 */
fun getAvailableYears(clock: Clock): List<Int> {
    val currentYear = getCurrentYear(clock)
    return (2024..currentYear).toList()
}

/**
 * Format current date and time for file naming.
 * Format: YYYYMMDD_HHmmss (e.g., 20260118_143025)
 * 
 * @param clock Clock instance to get current time
 * @return Formatted timestamp string
 */
fun formatTimestampForFileName(clock: Clock): String {
    val now = clock.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val year = now.year
    val month = now.month.number.toString().padStart(2, '0')
    val day = now.dayOfMonth.toString().padStart(2, '0')
    val hour = now.hour.toString().padStart(2, '0')
    val minute = now.minute.toString().padStart(2, '0')
    val second = now.second.toString().padStart(2, '0')
    return "${year}${month}${day}_${hour}${minute}${second}"
}
