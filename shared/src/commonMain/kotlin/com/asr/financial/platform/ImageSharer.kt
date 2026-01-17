package com.asr.financial.platform

/**
 * Platform-specific image sharing.
 * Allows sharing images via system share sheet (WhatsApp, email, etc.)
 */
expect class ImageSharer {
    /**
     * Share image data.
     * @param imageData PNG image data as ByteArray
     * @param fileName Suggested file name (e.g., "report.png")
     * @param title Share dialog title
     * @return true if share dialog was shown successfully
     */
    suspend fun shareImage(imageData: ByteArray, fileName: String, title: String): Boolean
}
