package com.asr.financial.platform

/**
 * Platform abstraction for file operations
 * Handles receipt image storage
 */
expect class FileHandler {
    suspend fun saveFile(fileName: String, data: ByteArray): String
    suspend fun readFile(filePath: String): ByteArray?
    suspend fun deleteFile(filePath: String): Boolean
    suspend fun fileExists(filePath: String): Boolean
    fun getFilePath(fileName: String): String
    /**
     * Clean up all temporary files with the given prefix in the system temp directory.
     * @param prefix File name prefix to match (e.g., "firebase_")
     * @return Number of files deleted
     */
    suspend fun cleanupTempFiles(prefix: String): Int
}
