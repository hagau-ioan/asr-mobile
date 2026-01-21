package com.asr.financial.platform

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android implementation of Clipboard.
 */
actual class Clipboard(private val context: Context) {
    actual suspend fun copyToClipboard(text: String): Boolean = withContext(Dispatchers.Main) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("ASR Financial", text)
            clipboard.setPrimaryClip(clip)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
