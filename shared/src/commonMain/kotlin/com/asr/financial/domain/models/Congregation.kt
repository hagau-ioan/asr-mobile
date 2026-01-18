package com.asr.financial.domain.models

import kotlinx.datetime.Instant

/**
 * Domain model for Congregation
 */
@Suppress("DEPRECATION") // kotlinx.datetime.Instant is the correct multiplatform type
data class Congregation(
    val id: Long = 0,
    val name: String,
    val location: String,
    val isActive: Boolean = true,
    val createdAt: Instant
)
