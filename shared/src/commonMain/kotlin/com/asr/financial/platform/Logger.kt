package com.asr.financial.platform

/**
 * Platform abstraction for logging
 * Development logs and crash reporting
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class Logger {
    fun debug(tag: String, message: String)
    fun info(tag: String, message: String)
    fun warning(tag: String, message: String)
    fun error(tag: String, message: String, throwable: Throwable? = null)
}
