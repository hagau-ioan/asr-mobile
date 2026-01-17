package com.asr.financial.platform

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Android implementation of ImageCapture.
 * Note: Actual camera launch must be done from Composable using rememberLauncherForActivityResult.
 */
actual class ImageCapture(private val context: Context) {
    private val receiptsDir = File(context.cacheDir, "receipts").apply { mkdirs() }
    private val currentReceiptFile = File(receiptsDir, "current_receipt.jpg")

    actual fun getReceiptFilePath(): String {
        return currentReceiptFile.absolutePath
    }

    actual fun getCurrentReceiptPath(): String? {
        return if (currentReceiptFile.exists()) currentReceiptFile.absolutePath else null
    }

    actual fun processCapture(success: Boolean): String? {
        if (!success || !currentReceiptFile.exists()) return null

        return try {
            val bitmap = BitmapFactory.decodeFile(currentReceiptFile.absolutePath)
            if (bitmap != null) {
                compressAndSave(bitmap, currentReceiptFile)
                bitmap.recycle()
                currentReceiptFile.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    actual fun deleteCurrentReceipt(): Boolean {
        return if (currentReceiptFile.exists()) {
            currentReceiptFile.delete()
        } else {
            false
        }
    }

    /**
     * Get the URI for camera intent (Android-specific).
     * Use this with FileProvider for camera capture.
     */
    fun getReceiptUri(): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            currentReceiptFile
        )
    }

    private fun compressAndSave(bitmap: Bitmap, destination: File) {
        val maxDimension = 1920
        val scale = minOf(
            maxDimension.toFloat() / bitmap.width,
            maxDimension.toFloat() / bitmap.height,
            1f
        )

        val scaledBitmap = if (scale < 1f) {
            val matrix = Matrix().apply { postScale(scale, scale) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }

        FileOutputStream(destination).use { out ->
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }

        if (scaledBitmap != bitmap) {
            scaledBitmap.recycle()
        }
    }
}
