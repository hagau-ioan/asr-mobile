package com.asr.financial

import com.asr.financial.platform.AppVersion

/**
 * App version information for presentation layer.
 * 
 * Version information:
 * - VERSION_NAME and VERSION_CODE are read from build configuration
 * - Android: Reads from BuildConfig (generated from libs.versions.toml/gradle.properties)
 * - iOS: Reads from Info.plist (CFBundleShortVersionString and CFBundleVersion)
 * - Update libs.versions.toml (appVersion, appVersionCode) or gradle.properties for Android
 * - Update Info.plist (CFBundleShortVersionString and CFBundleVersion) for iOS
 * 
 * Note: This is separate from AppConfig in shared module to avoid duplicate class conflicts.
 */
object AppVersionInfo {
    // App Version - Automatically read from build configuration
    // Single source of truth: libs.versions.toml (Android) and Info.plist (iOS)
    val VERSION_NAME: String = AppVersion.versionName
    val VERSION_CODE: Int = AppVersion.versionCode
}
