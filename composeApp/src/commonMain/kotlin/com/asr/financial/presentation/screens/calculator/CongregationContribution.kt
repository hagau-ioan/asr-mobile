package com.asr.financial.presentation.screens.calculator

/**
 * Presentation model for congregation contribution
 */
data class CongregationContribution(
    val congregationId: String,
    val congregationName: String,
    val numberOfPublishers: Int,
    val perPublisherAmount: Double,
    val totalAmount: Double
)
