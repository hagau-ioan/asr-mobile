package com.asr.financial

/**
 * App configuration constants for shared module.
 * 
 * Note: Version information (VERSION_NAME, VERSION_CODE) has been moved to
 * composeApp module since it's only needed in the presentation layer.
 */
object AppConfig {
    // Data source configuration
    // Set to true to use Firebase Cloud Storage (production)
    // Set to false to use local JSON files (testing)
    const val USE_FIREBASE_STORAGE: Boolean = true
}
