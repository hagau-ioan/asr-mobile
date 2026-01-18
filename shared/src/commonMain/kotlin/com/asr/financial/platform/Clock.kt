package com.asr.financial.platform

import kotlinx.datetime.Instant

/**
 * Platform abstraction for date/time operations
 * Handles timezone, locale-specific formatting (Romanian)
 */
@Suppress("DEPRECATION") // kotlinx.datetime.Instant is the correct multiplatform type, not kotlin.time.Instant
expect class Clock {
    fun now(): Instant
    fun formatDate(instant: Instant, pattern: String = "dd.MM.yyyy"): String
    fun formatDateTime(instant: Instant, pattern: String = "dd.MM.yyyy HH:mm"): String
    fun parseDate(dateString: String, pattern: String = "dd.MM.yyyy"): Instant?
}
