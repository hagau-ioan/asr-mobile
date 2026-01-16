package com.asr.financial.presentation.screens.yearly

/**
 * Presentation model for yearly financial summary
 */
data class YearlyStat(
    val year: Int,
    val totalDonations: Double,
    val totalExpenses: Double,
    val balance: Double
)
