package com.asr.financial.domain.models

import kotlinx.serialization.Serializable

/**
 * Simplified Congregation model for JSON serialization
 * Used for loading congregation data from JSON files
 */
@Serializable
data class CongregationInfo(
    val id: String,
    val name: String,
    val location: String,
    val memberCount: Int = 0,
    val monthlyCeiling: Double = 0.0,
    val isActive: Boolean = true
)
