package com.asr.financial.utils

import asr_financial.composeapp.generated.resources.*
import org.jetbrains.compose.resources.StringResource

/**
 * Reusable list of months with their string resources
 * Returns list of (monthNumber, StringResource) pairs
 * 
 * Usage:
 * ```kotlin
 * val months = getMonthsList()
 * val monthName = stringResource(months[0].second) // "Ianuarie"
 * ```
 */
fun getMonthsList(): List<Pair<Int, StringResource>> {
    return listOf(
        1 to Res.string.month_january,
        2 to Res.string.month_february,
        3 to Res.string.month_march,
        4 to Res.string.month_april,
        5 to Res.string.month_may,
        6 to Res.string.month_june,
        7 to Res.string.month_july,
        8 to Res.string.month_august,
        9 to Res.string.month_september,
        10 to Res.string.month_october,
        11 to Res.string.month_november,
        12 to Res.string.month_december
    )
}

/**
 * Get month name string resource by month number
 * 
 * @param monthNumber Month number (1-12)
 * @return StringResource for the month name, or null if invalid
 */
fun getMonthNameResource(monthNumber: Int): StringResource? {
    return when (monthNumber) {
        1 -> Res.string.month_january
        2 -> Res.string.month_february
        3 -> Res.string.month_march
        4 -> Res.string.month_april
        5 -> Res.string.month_may
        6 -> Res.string.month_june
        7 -> Res.string.month_july
        8 -> Res.string.month_august
        9 -> Res.string.month_september
        10 -> Res.string.month_october
        11 -> Res.string.month_november
        12 -> Res.string.month_december
        else -> null
    }
}
