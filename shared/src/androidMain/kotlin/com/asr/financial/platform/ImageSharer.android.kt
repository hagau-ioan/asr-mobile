package com.asr.financial.platform

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Android implementation of ImageSharer.
 */
actual class ImageSharer(private val context: Context) {
    actual suspend fun shareImage(imageData: ByteArray, fileName: String, title: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val imagesFolder = File(context.cacheDir, "images")
                imagesFolder.mkdirs()

                val file = File(imagesFolder, fileName)
                FileOutputStream(file).use { out ->
                    out.write(imageData)
                }

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, title)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                val chooser = Intent.createChooser(intent, title).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                context.startActivity(chooser)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
