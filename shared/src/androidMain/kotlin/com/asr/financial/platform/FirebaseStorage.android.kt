package com.asr.financial.platform

import com.google.firebase.storage.FirebaseStorage as AndroidFirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Android implementation of FirebaseStorage.
 * Uses Firebase Storage Android SDK to download files.
 * 
 * Note: Firebase Storage requires authentication. The SDK automatically uses
 * the current Firebase Auth user's token for authenticated requests.
 */
actual class FirebaseStorage actual constructor(
    private val logger: Logger
) {
    
    private val firebaseStorage: AndroidFirebaseStorage = AndroidFirebaseStorage.getInstance()
    
    /**
     * Maximum file size to download (10 MB)
     * Prevents OOM errors for large files
     */
    private companion object {
        const val MAX_DOWNLOAD_SIZE_BYTES: Long = 10 * 1024 * 1024 // 10 MB
        const val TAG = "FirebaseStorage"
    }
    
    actual suspend fun downloadFileAsString(path: String): String? = withContext(Dispatchers.IO) {
        try {
            logger.debug(TAG, "Downloading file from Firebase Storage: $path")
            
            // Verify user is authenticated before making request
            val firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                logger.error(TAG, "User not authenticated. Cannot download from Firebase Storage: $path")
                return@withContext null
            }
            
            // Get auth token to verify it's available
            try {
                val token = currentUser.getIdToken(false).await()
                logger.debug(TAG, "Auth token available for Firebase Storage request: $path")
            } catch (e: Exception) {
                logger.error(TAG, "Failed to get auth token for Firebase Storage: $path", e)
                return@withContext null
            }
            
            val storageRef = firebaseStorage.reference.child(path)
            
            // Try using getBytes() first (simpler, more reliable)
            try {
                logger.debug(TAG, "Attempting download using getBytes(): $path")
                val bytes = storageRef.getBytes(MAX_DOWNLOAD_SIZE_BYTES).await()
                val content = bytes.decodeToString()
                logger.debug(TAG, "Successfully downloaded file using getBytes(): $path (${bytes.size} bytes)")
                return@withContext content
            } catch (e: StorageException) {
                logger.warning(TAG, "getBytes() failed for $path, trying getFile() as fallback. Error: ${e.errorCode}")
                // Fall through to getFile() method
            }
            
            // Fallback to FileDownloadTask if getBytes() fails
            logger.debug(TAG, "Attempting download using getFile(): $path")
            val tempFile = java.io.File.createTempFile("firebase_download_", ".tmp")
            try {
                val downloadTask = storageRef.getFile(tempFile)
                downloadTask.await()
                
                if (!tempFile.exists() || tempFile.length() == 0L) {
                    logger.error(TAG, "Downloaded file is empty or doesn't exist: $path")
                    return@withContext null
                }
                
                if (tempFile.length() > MAX_DOWNLOAD_SIZE_BYTES) {
                    logger.error(TAG, "File exceeds maximum size: $path (${tempFile.length()} bytes)")
                    return@withContext null
                }
                
                val content = tempFile.readText()
                logger.debug(TAG, "Successfully downloaded file using getFile(): $path (${tempFile.length()} bytes)")
                content
            } finally {
                // Clean up temp file
                tempFile.delete()
            }
        } catch (e: StorageException) {
            // Firebase Storage specific errors
            val errorCode = e.errorCode
            val httpResultCode = e.httpResultCode
            val errorMessage = e.message ?: "Unknown Firebase Storage error"
            val cause = e.cause
            
            logger.error(TAG, "Firebase Storage error downloading $path:")
            logger.error(TAG, "  Error Code: $errorCode")
            logger.error(TAG, "  HTTP Result Code: $httpResultCode")
            logger.error(TAG, "  Error Message: $errorMessage")
            if (cause != null) {
                logger.error(TAG, "  Cause: ${cause.message}", cause)
            }
            
            when (errorCode) {
                StorageException.ERROR_OBJECT_NOT_FOUND -> {
                    logger.error(TAG, "File not found in Firebase Storage: $path")
                }
                StorageException.ERROR_QUOTA_EXCEEDED -> {
                    logger.error(TAG, "Firebase Storage quota exceeded")
                }
                StorageException.ERROR_NOT_AUTHENTICATED -> {
                    logger.error(TAG, "User not authenticated. Firebase Auth token required.")
                }
                StorageException.ERROR_UNKNOWN -> {
                    logger.error(TAG, "Unknown Firebase Storage error. HTTP Result: $httpResultCode")
                    logger.error(TAG, "This usually indicates a network issue or server configuration problem.")
                }
                else -> {
                    logger.error(TAG, "Firebase Storage error code: $errorCode, HTTP: $httpResultCode")
                }
            }
            null
        } catch (e: Exception) {
            logger.error(TAG, "Unexpected error downloading file from Firebase Storage: $path", e)
            null
        }
    }
}
