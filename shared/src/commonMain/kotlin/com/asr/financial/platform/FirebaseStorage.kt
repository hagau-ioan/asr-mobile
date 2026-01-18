package com.asr.financial.platform

/**
 * Platform abstraction for Firebase Cloud Storage.
 * Provides methods to download and upload files from/to Firebase Storage.
 * 
 * Actual implementations:
 * - Android: Uses Firebase Storage Android SDK
 * - iOS: Uses Firebase Storage iOS SDK via Swift bridge
 * 
 * Note: Firebase Storage requires authentication. The SDK automatically uses
 * the current Firebase Auth user's token for authenticated requests.
 */
expect class FirebaseStorage(logger: Logger) {
    /**
     * Download a file from Firebase Storage as a string.
     * 
     * @param path Path to the file in Firebase Storage (e.g., "app/app_config.json")
     * @return File content as string, or null if download fails
     */
    suspend fun downloadFileAsString(path: String): String?

    /**
     * Upload a file to Firebase Cloud Storage.
     * 
     * @param localPath Local file path to upload
     * @param remotePath Remote path in Firebase Storage (e.g., "documents/incoming/2026/01/receipt_20260118_143025.jpg")
     * @param onProgress Callback for upload progress (0.0 to 1.0)
     * @return true if upload successful, false otherwise
     */
    suspend fun uploadFile(
        localPath: String,
        remotePath: String,
        onProgress: (Float) -> Unit
    ): Boolean
}
