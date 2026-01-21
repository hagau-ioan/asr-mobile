package com.asr.financial.platform

/**
 * Platform-specific clipboard operations.
 */
expect class Clipboard {
    /**
     * Copy text to clipboard
     * @param text Text to copy
     * @return true if copy was successful
     */
    suspend fun copyToClipboard(text: String): Boolean
}
