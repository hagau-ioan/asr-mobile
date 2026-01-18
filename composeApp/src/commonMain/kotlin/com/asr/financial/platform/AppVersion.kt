package com.asr.financial.platform

/**
 * Platform abstraction for app version information.
 * Reads version from build configuration (BuildConfig on Android, Info.plist on iOS).
 * 
 * This allows the app to access version information that comes from gradle.properties
 * without duplicating the values in Kotlin code.
 */
expect object AppVersion {
    /**
     * Get the version name (e.g., "1.0.0")
     */
    val versionName: String
    
    /**
     * Get the version code (e.g., 1)
     */
    val versionCode: Int
}
