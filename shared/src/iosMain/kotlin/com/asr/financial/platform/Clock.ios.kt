package com.asr.financial.platform

import kotlinx.datetime.Instant
import kotlinx.datetime.toNSDate
import platform.Foundation.*

actual class Clock {
    private val locale = NSLocale(localeIdentifier = "ro_RO")
    private val timeZone = NSTimeZone.timeZoneWithName("Europe/Bucharest")!!
    
    actual fun now(): Instant {
        return kotlinx.datetime.Clock.System.now()
    }
    
    actual fun formatDate(instant: Instant, pattern: String): String {
        val formatter = NSDateFormatter().apply {
            dateFormat = pattern
            this.locale = this@Clock.locale
            this.timeZone = this@Clock.timeZone
        }
        return formatter.stringFromDate(instant.toNSDate())
    }
    
    actual fun formatDateTime(instant: Instant, pattern: String): String {
        val formatter = NSDateFormatter().apply {
            dateFormat = pattern
            this.locale = this@Clock.locale
            this.timeZone = this@Clock.timeZone
        }
        return formatter.stringFromDate(instant.toNSDate())
    }
    
    actual fun parseDate(dateString: String, pattern: String): Instant? {
        val formatter = NSDateFormatter().apply {
            dateFormat = pattern
            this.locale = this@Clock.locale
            this.timeZone = this@Clock.timeZone
        }
        val date = formatter.dateFromString(dateString) ?: return null
        return Instant.fromEpochMilliseconds((date.timeIntervalSince1970 * 1000).toLong())
    }
}
