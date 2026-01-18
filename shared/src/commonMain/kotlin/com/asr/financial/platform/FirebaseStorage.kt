package com.asr.financial.platform

/**
 * Platform abstraction for Firebase Cloud Storage.
 * Provides methods to download files from Firebase Storage.
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
}
