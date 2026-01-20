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

    actual suspend fun listFiles(path: String): List<CloudStorageFile> = withContext(Dispatchers.Main) {
        logger.debug("FirebaseStorage", "Listing files from Firebase Storage: $path")
        suspendCancellableCoroutine { continuation ->
            com.asr.financial.platform.FirebaseStorageBridge.listFiles(path) { files, error ->
                if (error != null) {
                    logger.error("FirebaseStorage", "Error listing files from $path: $error", null)
                    continuation.resume(emptyList())
                } else {
                    logger.debug("FirebaseStorage", "Successfully listed ${files.size} files from: $path")
                    continuation.resume(files)
                }
            }
        }
    }

    actual suspend fun deleteFile(remotePath: String): Boolean = withContext(Dispatchers.Main) {
        logger.debug("FirebaseStorage", "Deleting file from Firebase Storage: $remotePath")
        suspendCancellableCoroutine { continuation ->
            com.asr.financial.platform.FirebaseStorageBridge.deleteFile(remotePath) { success, error ->
                if (error != null) {
                    logger.error("FirebaseStorage", "Error deleting $remotePath: $error", null)
                } else if (success) {
                    logger.debug("FirebaseStorage", "Successfully deleted file: $remotePath")
                } else {
                    logger.error("FirebaseStorage", "Delete returned false for: $remotePath", null)
                }
                continuation.resume(success)
            }
        }
    }

    actual suspend fun downloadFileToTemp(remotePath: String): String? = withContext(Dispatchers.Main) {
        logger.debug("FirebaseStorage", "Downloading file to temp from Firebase Storage: $remotePath")
        suspendCancellableCoroutine { continuation ->
            com.asr.financial.platform.FirebaseStorageBridge.downloadFileToTemp(remotePath) { localPath, error ->
                if (error != null) {
                    logger.error("FirebaseStorage", "Error downloading to temp $remotePath: $error", null)
                } else if (localPath != null) {
                    logger.debug("FirebaseStorage", "Successfully downloaded to temp: $localPath")
                } else {
                    logger.error("FirebaseStorage", "Download to temp returned null for: $remotePath", null)
                }
                continuation.resume(localPath)
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

    // List files callback
    private var listFilesCallback: ((List<CloudStorageFile>, String?) -> Unit)? = null

    /**
     * Called from Kotlin to list files from a Firebase Storage directory.
     * The Swift bridge should observe this and call Firebase Storage.
     */
    fun listFiles(
        path: String,
        completion: (List<CloudStorageFile>, String?) -> Unit
    ) {
        listFilesCallback = completion
        // Post notification to Swift layer
        platform.Foundation.NSNotificationCenter.defaultCenter.postNotificationName(
            "FirebaseStorageListFiles",
            `object` = mapOf("path" to path)
        )
    }

    /**
     * Called from Swift bridge when list files completes.
     */
    fun reportListFilesResult(files: List<CloudStorageFile>, error: String?) {
        listFilesCallback?.invoke(files, error)
        listFilesCallback = null
    }

    // Delete file callback
    private var deleteFileCallback: ((Boolean, String?) -> Unit)? = null

    /**
     * Called from Kotlin to delete a file from Firebase Storage.
     * The Swift bridge should observe this and call Firebase Storage.
     */
    fun deleteFile(
        remotePath: String,
        completion: (Boolean, String?) -> Unit
    ) {
        deleteFileCallback = completion
        // Post notification to Swift layer
        platform.Foundation.NSNotificationCenter.defaultCenter.postNotificationName(
            "FirebaseStorageDeleteFile",
            `object` = mapOf("remotePath" to remotePath)
        )
    }

    /**
     * Called from Swift bridge when file delete completes.
     */
    fun reportDeleteFileResult(success: Boolean, error: String?) {
        deleteFileCallback?.invoke(success, error)
        deleteFileCallback = null
    }

    // Download to temp callback
    private var downloadToTempCallback: ((String?, String?) -> Unit)? = null

    /**
     * Called from Kotlin to download a file to a temp location.
     * The Swift bridge should observe this and call Firebase Storage.
     */
    fun downloadFileToTemp(
        remotePath: String,
        completion: (String?, String?) -> Unit
    ) {
        downloadToTempCallback = completion
        // Post notification to Swift layer
        platform.Foundation.NSNotificationCenter.defaultCenter.postNotificationName(
            "FirebaseStorageDownloadToTemp",
            `object` = mapOf("remotePath" to remotePath)
        )
    }

    /**
     * Called from Swift bridge when download to temp completes.
     */
    fun reportDownloadToTempResult(localPath: String?, error: String?) {
        downloadToTempCallback?.invoke(localPath, error)
        downloadToTempCallback = null
    }
}
