@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.asr.financial.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * iOS implementation of FirebaseStorage using a bridge pattern.
 * 
 * This uses FirebaseStorageBridge (Swift) which must be called from the iOS app layer.
 * The bridge is accessed via a singleton pattern similar to FirebaseAuthBridge.
 * 
 * Note: The Swift bridge (FirebaseStorageBridge.swift) must be added to the Xcode project
 * and the Firebase Storage iOS SDK must be added via Swift Package Manager.
 * 
 * Note: Firebase Storage requires authentication. The SDK automatically uses
 * the current Firebase Auth user's token for authenticated requests.
 */
actual class FirebaseStorage(
    private val logger: Logger
) {
    
    actual suspend fun downloadFileAsString(path: String): String? = withContext(Dispatchers.Main) {
        logger.debug("FirebaseStorage", "Downloading file from Firebase Storage: $path")
        suspendCancellableCoroutine { continuation ->
            com.asr.financial.platform.FirebaseStorageBridge.downloadFile(path) { content, error ->
                if (error != null) {
                    logger.error("FirebaseStorage", "Error downloading $path: $error", null)
                } else if (content != null) {
                    logger.debug("FirebaseStorage", "Successfully downloaded file: $path")
                } else {
                    logger.error("FirebaseStorage", "Download returned null for: $path", null)
                }
                continuation.resume(content)
            }
        }
    }
}

/**
 * Bridge object for iOS Firebase Storage communication.
 * This object provides static methods that will be called by the Swift bridge.
 * The Swift FirebaseStorageBridge class should call these methods.
 * 
 * Pattern similar to FirebaseAuthBridge in the codebase.
 */
object FirebaseStorageBridge {
    
    // Download file callback
    private var downloadFileCallback: ((String?, String?) -> Unit)? = null
    
    /**
     * Called from Kotlin to download a file from Firebase Storage.
     * The Swift bridge should observe this and call Firebase Storage.
     */
    fun downloadFile(
        path: String,
        completion: (String?, String?) -> Unit
    ) {
        downloadFileCallback = completion
        // Post notification to Swift layer
        platform.Foundation.NSNotificationCenter.defaultCenter.postNotificationName(
            "FirebaseStorageDownloadFile",
            `object` = mapOf("path" to path)
        )
    }
    
    /**
     * Called from Swift bridge when file download completes.
     */
    fun reportDownloadResult(content: String?, error: String?) {
        downloadFileCallback?.invoke(content, error)
        downloadFileCallback = null
    }
}
