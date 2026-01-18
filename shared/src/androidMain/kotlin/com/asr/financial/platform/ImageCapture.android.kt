package com.asr.financial.platform

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
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
            // Read EXIF orientation before decoding bitmap
            val orientation = getExifOrientation(currentReceiptFile)
            
            val bitmap = BitmapFactory.decodeFile(currentReceiptFile.absolutePath)
            if (bitmap != null) {
                // Apply rotation based on EXIF orientation
                val rotatedBitmap = applyOrientation(bitmap, orientation)
                compressAndSave(rotatedBitmap, currentReceiptFile)
                
                // Update EXIF orientation to NORMAL since we've applied the rotation to the bitmap
                if (orientation != ExifInterface.ORIENTATION_NORMAL) {
                    updateExifOrientation(currentReceiptFile, ExifInterface.ORIENTATION_NORMAL)
                }
                
                // Recycle bitmaps
                if (rotatedBitmap != bitmap) {
                    rotatedBitmap.recycle()
                }
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

    actual fun renameReceiptWithTimestamp(timestamp: String): String? {
        if (!currentReceiptFile.exists()) return null
        
        return try {
            val newFileName = "receipt_$timestamp.jpg"
            val newFile = File(receiptsDir, newFileName)
            
            if (currentReceiptFile.renameTo(newFile)) {
                newFile.absolutePath
            } else {
                // If rename fails, try copy and delete
                currentReceiptFile.copyTo(newFile, overwrite = true)
                currentReceiptFile.delete()
                newFile.absolutePath
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

    /**
     * Get EXIF orientation value from image file.
     * Returns ORIENTATION_NORMAL if orientation cannot be determined.
     */
    private fun getExifOrientation(file: File): Int {
        return try {
            ExifInterface(file.absolutePath).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        } catch (e: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }
    }

    /**
     * Update EXIF orientation in image file.
     */
    private fun updateExifOrientation(file: File, orientation: Int) {
        try {
            val exif = ExifInterface(file.absolutePath)
            exif.setAttribute(ExifInterface.TAG_ORIENTATION, orientation.toString())
            exif.saveAttributes()
        } catch (e: Exception) {
            // Ignore errors updating EXIF
        }
    }

    /**
     * Apply EXIF orientation to bitmap by rotating/flipping as needed.
     * Returns a new bitmap if rotation is needed, otherwise returns the original.
     */
    private fun applyOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        
        when (orientation) {
            ExifInterface.ORIENTATION_NORMAL -> return bitmap
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> {
                matrix.setScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_180 -> {
                matrix.setRotate(180f)
            }
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                matrix.setRotate(180f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> {
                matrix.setRotate(90f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(-90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> {
                matrix.setRotate(-90f)
            }
            else -> return bitmap
        }
        
        return try {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: OutOfMemoryError) {
            // If rotation fails due to memory, return original
            bitmap
        }
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
