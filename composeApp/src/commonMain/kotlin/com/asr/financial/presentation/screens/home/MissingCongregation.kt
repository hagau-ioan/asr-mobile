package com.asr.financial.presentation.screens.home

/**
 * Presentation model for a congregation that hasn't met its expected donation.
 */
data class MissingCongregation(
    val name: String,
    val donated: Double,
    val expected: Double,
    val missing: Double
)
