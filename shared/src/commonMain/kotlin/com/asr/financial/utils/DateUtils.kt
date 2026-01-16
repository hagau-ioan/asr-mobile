package com.asr.financial.utils

import com.asr.financial.platform.Clock
import kotlinx.datetime.TimeZone
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
        .monthNumber
}

/**
 * Generate available years from 2024 to current year
 */
fun getAvailableYears(clock: Clock): List<Int> {
    val currentYear = getCurrentYear(clock)
    return (2024..currentYear).toList()
}
