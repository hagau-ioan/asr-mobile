package com.asr.financial.presentation.screens.yearly

/**
 * Presentation model for year-over-year comparison
 */
data class YearlyComparison(
    val currentYear: Int,
    val previousYear: Int,
    val donationsChange: Double,
    val donationsChangePercent: Double,
    val expensesChange: Double,
    val expensesChangePercent: Double
)
