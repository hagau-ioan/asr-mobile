package com.asr.financial.domain.models

import kotlinx.serialization.Serializable

/**
 * Domain model for Application Configuration
 */
@Serializable
data class AppConfig(
    val organization: OrganizationConfig,
    val financial: FinancialConfig
)

@Serializable
data class OrganizationConfig(
    val name: String,
    val subtitle: String,
    val location: String
)

@Serializable
data class FinancialConfig(
    val expectedDonationPerCongregation: Double,
    val totalPublishers: Int,
    val publisherExpectedContribution: Double = 30.0,
    val currency: String,
    val startYear: Int = 2024
)
