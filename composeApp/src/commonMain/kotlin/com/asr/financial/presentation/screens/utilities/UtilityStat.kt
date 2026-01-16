package com.asr.financial.presentation.screens.utilities

/**
 * Presentation model for utility expense statistics
 */
data class UtilityStat(
    val category: String,
    val amount: Double,
    val percentage: Int
)
