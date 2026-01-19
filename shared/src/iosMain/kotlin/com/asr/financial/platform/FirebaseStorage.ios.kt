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
actual class FirebaseStorage actual constructor(
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

    actual suspend fun uploadFile(
        localPath: String,
        remotePath: String,
        onProgress: (Float) -> Unit
    ): Boolean = withContext(Dispatchers.Main) {
        logger.debug("FirebaseStorage", "Uploading file to Firebase Storage: $localPath -> $remotePath")
        suspendCancellableCoroutine { continuation ->
            com.asr.financial.platform.FirebaseStorageBridge.uploadFile(
                localPath = localPath,
                remotePath = remotePath,
                onProgress = onProgress
            ) { success, error ->
                if (error != null) {
                    logger.error("FirebaseStorage", "Error uploading $remotePath: $error", null)
                } else if (success) {
                    logger.debug("FirebaseStorage", "Successfully uploaded file: $remotePath")
                } else {
                    logger.error("FirebaseStorage", "Upload returned false for: $remotePath", null)
                }
                continuation.resume(success)
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
    
    // Track callbacks by path to handle concurrent requests
    // Since we're on Main thread, we can use simple mutable map
    private val pendingCallbacks = mutableMapOf<String, MutableList<(String?, String?) -> Unit>>()
    private var currentDownloadPath: String? = null
    
    /**
     * Called from Kotlin to download a file from Firebase Storage.
     * The Swift bridge should observe this and call Firebase Storage.
     * Queues callbacks for concurrent requests to the same path.
     */
    fun downloadFile(
        path: String,
        completion: (String?, String?) -> Unit
    ) {
        // Add callback to queue for this path
        val callbacks = pendingCallbacks.getOrPut(path) { mutableListOf() }
        callbacks.add(completion)
        
        // Only trigger download if this is the first request for this path
        if (callbacks.size == 1) {
            currentDownloadPath = path
            // Post notification to Swift layer
            platform.Foundation.NSNotificationCenter.defaultCenter.postNotificationName(
                "FirebaseStorageDownloadFile",
                `object` = mapOf("path" to path)
            )
        }
    }
    
    /**
     * Called from Swift bridge when file download completes.
     * Invokes all pending callbacks for the specified path.
     */
    fun reportDownloadResult(path: String, content: String?, error: String?) {
        val callbacks = pendingCallbacks.remove(path)
        callbacks?.forEach { callback ->
            callback(content, error)
        }
        // Clear current path if it matches
        if (currentDownloadPath == path) {
            currentDownloadPath = null
        }
    }

    // Upload file callback
    private var uploadFileCallback: ((Boolean, String?) -> Unit)? = null
    private var uploadProgressCallback: ((Float) -> Unit)? = null

    /**
     * Called from Kotlin to upload a file to Firebase Storage.
     * The Swift bridge should observe this and call Firebase Storage.
     */
    fun uploadFile(
        localPath: String,
        remotePath: String,
        onProgress: (Float) -> Unit,
        completion: (Boolean, String?) -> Unit
    ) {
        uploadProgressCallback = onProgress
        uploadFileCallback = completion
        // Post notification to Swift layer
        platform.Foundation.NSNotificationCenter.defaultCenter.postNotificationName(
            "FirebaseStorageUploadFile",
            `object` = mapOf(
                "localPath" to localPath,
                "remotePath" to remotePath
            )
        )
    }

    /**
     * Called from Swift bridge when upload progress updates.
     */
    fun reportUploadProgress(progress: Float) {
        uploadProgressCallback?.invoke(progress)
    }

    /**
     * Called from Swift bridge when file upload completes.
     */
    fun reportUploadResult(success: Boolean, error: String?) {
        uploadFileCallback?.invoke(success, error)
        uploadFileCallback = null
        uploadProgressCallback = null
    }
}
