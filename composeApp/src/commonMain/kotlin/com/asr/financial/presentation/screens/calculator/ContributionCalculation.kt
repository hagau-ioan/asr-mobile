package com.asr.financial.presentation.screens.calculator

/**
 * Presentation model for contribution calculation
 */
data class ContributionCalculation(
    val totalExpenses: Double,
    val numberOfPublishers: Int,
    val perPublisherAmount: Double
)
