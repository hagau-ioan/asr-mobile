package com.asr.financial.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.UIKit.UIPasteboard
import platform.UIKit.generalPasteboard

/**
 * iOS implementation of Clipboard.
 */
actual class Clipboard {
    actual suspend fun copyToClipboard(text: String): Boolean = withContext(Dispatchers.Main) {
        try {
            val pasteboard = UIPasteboard.generalPasteboard
            pasteboard.string = text
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
