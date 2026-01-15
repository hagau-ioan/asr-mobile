package com.asr.financial.platform

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

actual class Clock {
    private val romanianLocale = Locale("ro", "RO")
    private val zoneId = ZoneId.of("Europe/Bucharest")
    
    actual fun now(): Instant {
        return kotlinx.datetime.Clock.System.now()
    }
    
    actual fun formatDate(instant: Instant, pattern: String): String {
        val formatter = DateTimeFormatter.ofPattern(pattern, romanianLocale)
            .withZone(zoneId)
        return formatter.format(instant.toJavaInstant())
    }
    
    actual fun formatDateTime(instant: Instant, pattern: String): String {
        val formatter = DateTimeFormatter.ofPattern(pattern, romanianLocale)
            .withZone(zoneId)
        return formatter.format(instant.toJavaInstant())
    }
    
    actual fun parseDate(dateString: String, pattern: String): Instant? {
        return try {
            val formatter = DateTimeFormatter.ofPattern(pattern, romanianLocale)
                .withZone(zoneId)
            java.time.Instant.from(formatter.parse(dateString)).toKotlinInstant()
        } catch (e: Exception) {
            null
        }
    }
}
