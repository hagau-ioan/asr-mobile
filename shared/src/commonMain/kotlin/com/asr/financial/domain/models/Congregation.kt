package com.asr.financial.domain.models

import kotlinx.datetime.Instant

/**
 * Domain model for Congregation
 */
data class Congregation(
    val id: Long = 0,
    val name: String,
    val location: String,
    val isActive: Boolean = true,
    val createdAt: Instant
)
