package com.asr.financial.presentation.screens.utilities

/**
 * Presentation model for utility comparison statistics
 */
data class UtilityComparison(
    val category: String,
    val currentAmount: Double,
    val previousAmount: Double,
    val difference: Double,
    val percentageChange: Double
)
