package com.asr.financial.platform

/**
 * Platform-specific image capture and compression.
 * Uses String paths instead of File for cross-platform compatibility.
 */
expect class ImageCapture {
    /**
     * Get the file path where camera image should be saved.
     * The actual camera launch must happen in the UI layer.
     * @return Path to the receipt file location
     */
    fun getReceiptFilePath(): String

    /**
     * Get current cached receipt image path if exists
     * @return Path to current receipt or null if none exists
     */
    fun getCurrentReceiptPath(): String?

    /**
     * Process captured image (compress and save)
     * @param success Whether the capture was successful
     * @return Path to processed image or null if failed
     */
    fun processCapture(success: Boolean): String?

    /**
     * Rename current receipt file with timestamp.
     * Format: receipt_YYYYMMDD_HHmmss.jpg
     * @param timestamp Timestamp string in format YYYYMMDD_HHmmss
     * @return Path to renamed file or null if rename failed
     */
    fun renameReceiptWithTimestamp(timestamp: String): String?

    /**
     * Delete current receipt from cache
     * @return true if deleted successfully
     */
    fun deleteCurrentReceipt(): Boolean
}
