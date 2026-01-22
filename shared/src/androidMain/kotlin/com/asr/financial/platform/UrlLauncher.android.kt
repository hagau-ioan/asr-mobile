package com.asr.financial.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android implementation of UrlLauncher.
 * Uses Intent with ACTION_VIEW to open URLs in the default browser.
 */
actual class UrlLauncher(private val context: Context) {
    
    actual suspend fun openUrl(url: String): Boolean = withContext(Dispatchers.Main) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ContextCompat.startActivity(context, intent, null)
            true
        } catch (e: Exception) {
            false
        }
    }
}
