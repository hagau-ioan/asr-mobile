@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.asr.financial.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.UIKit.UIApplication
import platform.Foundation.NSURL
import kotlin.coroutines.resume

/**
 * iOS implementation of UrlLauncher.
 * Uses UIApplication.shared.open(_:options:completionHandler:) to open URLs in Safari or appropriate app.
 * Note: openURL is deprecated since iOS 10, so we use the modern API.
 */
actual class UrlLauncher {
    
    actual suspend fun openUrl(url: String): Boolean = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
            try {
                val nsUrl = NSURL(string = url)
                val app = UIApplication.sharedApplication
                
                // Use modern API: open(_:options:completionHandler:) instead of deprecated openURL
                // Empty map for options (no special options needed)
                app.openURL(
                    url = nsUrl,
                    options = emptyMap<Any?, Any>(),
                    completionHandler = { success: Boolean ->
                        continuation.resume(success)
                    }
                )
            } catch (e: Exception) {
                continuation.resume(false)
            }
        }
    }
}
