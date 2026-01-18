package com.asr.financial.platform

import platform.Foundation.NSBundle
import platform.Foundation.objectForKey

/**
 * iOS implementation of AppVersion.
 * Reads from Info.plist (CFBundleShortVersionString and CFBundleVersion).
 */
actual object AppVersion {
    actual val versionName: String
        get() {
            val bundle = NSBundle.mainBundle
            return (bundle.objectForKey("CFBundleShortVersionString") as? String) ?: "1.0.0"
        }
    
    actual val versionCode: Int
        get() {
            val bundle = NSBundle.mainBundle
            val versionString = (bundle.objectForKey("CFBundleVersion") as? String) ?: "1"
            return versionString.toIntOrNull() ?: 1
        }
}
