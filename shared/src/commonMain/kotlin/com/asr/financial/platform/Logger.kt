package com.asr.financial.platform

/**
 * Platform abstraction for logging
 * Development logs and crash reporting
 */
expect class Logger {
    fun debug(tag: String, message: String)
    fun info(tag: String, message: String)
    fun warning(tag: String, message: String)
    fun error(tag: String, message: String, throwable: Throwable? = null)
}
