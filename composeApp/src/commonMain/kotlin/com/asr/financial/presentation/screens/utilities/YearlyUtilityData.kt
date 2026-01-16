package com.asr.financial.presentation.screens.utilities

/**
 * Presentation model for yearly utility data point
 */
data class YearlyUtilityData(
    val month: Int,
    val currentYearAmount: Double,
    val previousYearAmount: Double
)
