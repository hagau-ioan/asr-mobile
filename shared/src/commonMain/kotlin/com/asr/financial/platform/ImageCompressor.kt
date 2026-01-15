package com.asr.financial.platform

/**
 * Platform abstraction for image compression
 * Max width: 1920px, JPEG quality: 85%
 */
expect class ImageCompressor {
    suspend fun compress(
        imageData: ByteArray,
        maxWidth: Int = 1920,
        quality: Int = 85
    ): ByteArray
}
