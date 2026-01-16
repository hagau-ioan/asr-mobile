package com.asr.financial.presentation.screens.congregations

/**
 * Presentation model for congregation donation statistics
 */
data class CongregationStat(
    val name: String,
    val donated: Double,
    val expected: Double,
    val difference: Double,
    val lastDonation: String?,
    val isMissing: Boolean
)
