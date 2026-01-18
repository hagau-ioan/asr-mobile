package com.asr.financial.platform

/**
 * Android implementation of AppVersion.
 * Reads from BuildConfig which is generated from libs.versions.toml/gradle.properties.
 * 
 * Pattern matches preachtrack project: uses fully qualified name instead of import.
 */
actual object AppVersion {
    actual val versionName: String = com.asr.financial.BuildConfig.VERSION_NAME
    
    actual val versionCode: Int = com.asr.financial.BuildConfig.VERSION_CODE
}
