package com.asr.financial.platform

/**
 * Represents a file stored in Firebase Cloud Storage.
 * Used for listing and displaying uploaded documents.
 */
data class CloudStorageFile(
    /** Full path to the file in Firebase Storage */
    val path: String,
    /** File name (extracted from path) */
    val name: String,
    /** File size in bytes */
    val sizeBytes: Long,
    /** Last modified timestamp in milliseconds since epoch */
    val updatedTimeMillis: Long
) {
    /**
     * Returns the file size in a human-readable format.
     */
    fun formattedSize(): String {
        return when {
            sizeBytes < 1024 -> "$sizeBytes B"
            sizeBytes < 1024 * 1024 -> "${sizeBytes / 1024} KB"
            else -> "${sizeBytes / (1024 * 1024)} MB"
        }
    }
}
