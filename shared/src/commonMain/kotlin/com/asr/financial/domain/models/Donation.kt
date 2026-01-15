package com.asr.financial.domain.models

import kotlinx.datetime.Instant

/**
 * Domain model for Donation
 */
data class Donation(
    val id: Long = 0,
    val congregationId: Long,
    val amount: Double,
    val month: Int,
    val year: Int,
    val notes: String? = null,
    val createdAt: Instant
)
