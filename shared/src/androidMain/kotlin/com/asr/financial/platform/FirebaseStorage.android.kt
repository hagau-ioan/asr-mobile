package com.asr.financial.platform

import com.google.firebase.storage.FirebaseStorage as AndroidFirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

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

    actual suspend fun uploadFile(
        localPath: String,
        remotePath: String,
        onProgress: (Float) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            logger.debug(TAG, "Uploading file to Firebase Storage: $localPath -> $remotePath")

            // Verify user is authenticated
            val firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                logger.error(TAG, "User not authenticated. Cannot upload to Firebase Storage: $remotePath")
                return@withContext false
            }

            // Get auth token to verify it's available
            try {
                val token = currentUser.getIdToken(false).await()
                logger.debug(TAG, "Auth token available for Firebase Storage upload: $remotePath")
            } catch (e: Exception) {
                logger.error(TAG, "Failed to get auth token for Firebase Storage upload: $remotePath", e)
                return@withContext false
            }

            val localFile = java.io.File(localPath)
            if (!localFile.exists()) {
                logger.error(TAG, "Local file does not exist: $localPath")
                return@withContext false
            }

            val storageRef = firebaseStorage.reference.child(remotePath)
            val fileUri = android.net.Uri.fromFile(localFile)
            val uploadTask = storageRef.putFile(fileUri)

            // Monitor upload progress
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (taskSnapshot.bytesTransferred.toFloat() / taskSnapshot.totalByteCount.toFloat())
                onProgress(progress)
            }

            // Wait for upload to complete
            uploadTask.await()
            
            logger.debug(TAG, "Successfully uploaded file: $remotePath (${localFile.length()} bytes)")
            true
        } catch (e: StorageException) {
            val errorCode = e.errorCode
            val httpResultCode = e.httpResultCode
            val errorMessage = e.message ?: "Unknown Firebase Storage error"

            logger.error(TAG, "Firebase Storage error uploading $remotePath:")
            logger.error(TAG, "  Error Code: $errorCode")
            logger.error(TAG, "  HTTP Result Code: $httpResultCode")
            logger.error(TAG, "  Error Message: $errorMessage")

            when (errorCode) {
                StorageException.ERROR_QUOTA_EXCEEDED -> {
                    logger.error(TAG, "Firebase Storage quota exceeded")
                }
                StorageException.ERROR_NOT_AUTHENTICATED -> {
                    logger.error(TAG, "User not authenticated. Firebase Auth token required.")
                }
                StorageException.ERROR_UNKNOWN -> {
                    logger.error(TAG, "Unknown Firebase Storage error. HTTP Result: $httpResultCode")
                }
                else -> {
                    logger.error(TAG, "Firebase Storage error code: $errorCode, HTTP: $httpResultCode")
                }
            }
            false
        } catch (e: Exception) {
            logger.error(TAG, "Unexpected error uploading file to Firebase Storage: $remotePath", e)
            false
        }
    }

    actual suspend fun listFiles(path: String): List<CloudStorageFile> = withContext(Dispatchers.IO) {
        try {
            logger.debug(TAG, "Listing files in Firebase Storage: $path")

            // Verify user is authenticated
            val firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                logger.error(TAG, "User not authenticated. Cannot list files from Firebase Storage: $path")
                return@withContext emptyList()
            }

            val storageRef = firebaseStorage.reference.child(path)
            val listResult = storageRef.listAll().await()

            val files = mutableListOf<CloudStorageFile>()
            for (item in listResult.items) {
                try {
                    val metadata = item.metadata.await()
                    files.add(
                        CloudStorageFile(
                            path = item.path,
                            name = item.name,
                            sizeBytes = metadata.sizeBytes,
                            updatedTimeMillis = metadata.updatedTimeMillis
                        )
                    )
                } catch (e: Exception) {
                    logger.warning(TAG, "Failed to get metadata for ${item.path}: ${e.message}")
                    // Add file with default values if metadata fails
                    files.add(
                        CloudStorageFile(
                            path = item.path,
                            name = item.name,
                            sizeBytes = 0L,
                            updatedTimeMillis = 0L
                        )
                    )
                }
            }

            // Sort by updated time descending (newest first)
            files.sortByDescending { it.updatedTimeMillis }
            logger.debug(TAG, "Successfully listed ${files.size} files from: $path")
            files
        } catch (e: StorageException) {
            val errorCode = e.errorCode
            logger.error(TAG, "Firebase Storage error listing $path: Error Code $errorCode")
            emptyList()
        } catch (e: Exception) {
            logger.error(TAG, "Unexpected error listing files from Firebase Storage: $path", e)
            emptyList()
        }
    }

    actual suspend fun deleteFile(remotePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            logger.debug(TAG, "Deleting file from Firebase Storage: $remotePath")

            // Verify user is authenticated
            val firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                logger.error(TAG, "User not authenticated. Cannot delete from Firebase Storage: $remotePath")
                return@withContext false
            }

            val storageRef = firebaseStorage.reference.child(remotePath)
            storageRef.delete().await()

            logger.debug(TAG, "Successfully deleted file: $remotePath")
            true
        } catch (e: StorageException) {
            val errorCode = e.errorCode
            val errorMessage = e.message ?: "Unknown Firebase Storage error"
            logger.error(TAG, "Firebase Storage error deleting $remotePath: Error Code $errorCode - $errorMessage")
            false
        } catch (e: Exception) {
            logger.error(TAG, "Unexpected error deleting file from Firebase Storage: $remotePath", e)
            false
        }
    }

    actual suspend fun downloadFileToTemp(remotePath: String): String? = withContext(Dispatchers.IO) {
        try {
            logger.debug(TAG, "Downloading file to temp from Firebase Storage: $remotePath")

            // Verify user is authenticated
            val firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                logger.error(TAG, "User not authenticated. Cannot download from Firebase Storage: $remotePath")
                return@withContext null
            }

            val storageRef = firebaseStorage.reference.child(remotePath)

            // Create temp file with appropriate extension
            val fileName = remotePath.substringAfterLast("/")
            val extension = fileName.substringAfterLast(".", "tmp")
            val tempFile = File.createTempFile("firebase_", ".$extension")

            storageRef.getFile(tempFile).await()

            if (!tempFile.exists() || tempFile.length() == 0L) {
                logger.error(TAG, "Downloaded file is empty or doesn't exist: $remotePath")
                tempFile.delete()
                return@withContext null
            }

            logger.debug(TAG, "Successfully downloaded file to temp: ${tempFile.absolutePath}")
            tempFile.absolutePath
        } catch (e: StorageException) {
            val errorCode = e.errorCode
            logger.error(TAG, "Firebase Storage error downloading $remotePath: Error Code $errorCode")
            null
        } catch (e: Exception) {
            logger.error(TAG, "Unexpected error downloading file from Firebase Storage: $remotePath", e)
            null
        }
    }
}
