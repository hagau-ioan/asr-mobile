package com.asr.financial.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.Foundation.*
import platform.UIKit.*
import platform.CoreGraphics.*

/**
 * iOS implementation of ImageCapture.
 * Note: Actual camera presentation must be done from SwiftUI/UIKit layer.
 */
@OptIn(ExperimentalForeignApi::class)
actual class ImageCapture {
    private val receiptsDir: String
    private val currentReceiptPath: String

    init {
        val tempDir = NSTemporaryDirectory()
        receiptsDir = "${tempDir}receipts/"

        val fileManager = NSFileManager.defaultManager
        fileManager.createDirectoryAtPath(
            receiptsDir,
            withIntermediateDirectories = true,
            attributes = null,
            error = null
        )

        currentReceiptPath = "${receiptsDir}current_receipt.jpg"
    }

    actual fun getReceiptFilePath(): String {
        return currentReceiptPath
    }

    actual fun getCurrentReceiptPath(): String? {
        val fileManager = NSFileManager.defaultManager
        return if (fileManager.fileExistsAtPath(currentReceiptPath)) {
            currentReceiptPath
        } else {
            null
        }
    }

    actual fun processCapture(success: Boolean): String? {
        if (!success) return null

        val fileManager = NSFileManager.defaultManager
        return if (fileManager.fileExistsAtPath(currentReceiptPath)) {
            currentReceiptPath
        } else {
            null
        }
    }

    actual fun renameReceiptWithTimestamp(timestamp: String): String? {
        val fileManager = NSFileManager.defaultManager
        if (!fileManager.fileExistsAtPath(currentReceiptPath)) return null

        return try {
            val newFileName = "receipt_$timestamp.jpg"
            val newFilePath = "${receiptsDir}$newFileName"

            val success = fileManager.moveItemAtPath(
                currentReceiptPath,
                toPath = newFilePath,
                error = null
            )

            if (success) {
                newFilePath
            } else {
                // If move fails, try copy and delete
                val copySuccess = fileManager.copyItemAtPath(
                    currentReceiptPath,
                    toPath = newFilePath,
                    error = null
                )
                if (copySuccess) {
                    fileManager.removeItemAtPath(currentReceiptPath, error = null)
                    newFilePath
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    actual fun deleteCurrentReceipt(): Boolean {
        val fileManager = NSFileManager.defaultManager
        return if (fileManager.fileExistsAtPath(currentReceiptPath)) {
            fileManager.removeItemAtPath(currentReceiptPath, error = null)
        } else {
            false
        }
    }

    /**
     * Save image data to receipt file (iOS-specific).
     * Call this from UIImagePickerController delegate.
     */
    fun saveImageData(image: UIImage): String? {
        val compressed = compressImage(image)
        val data = UIImageJPEGRepresentation(compressed, 0.85)

        return if (data != null && data.writeToFile(currentReceiptPath, atomically = true)) {
            currentReceiptPath
        } else {
            null
        }
    }

    private fun compressImage(image: UIImage): UIImage {
        // First, normalize orientation by drawing the image to a new context
        // This ensures the image is in .up orientation regardless of how it was captured
        val normalizedImage = normalizeImageOrientation(image)
        
        val maxDimension = 1920.0
        val size = normalizedImage.size

        val scale = minOf(
            maxDimension / size.useContents { width },
            maxDimension / size.useContents { height },
            1.0
        )

        if (scale >= 1.0) return normalizedImage

        val newSize = CGSizeMake(
            size.useContents { width } * scale,
            size.useContents { height } * scale
        )

        UIGraphicsBeginImageContextWithOptions(newSize, false, 1.0)
        normalizedImage.drawInRect(CGRectMake(0.0, 0.0, newSize.useContents { width }, newSize.useContents { height }))
        val scaledImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()

        return scaledImage ?: normalizedImage
    }
    
    /**
     * Normalize image orientation by drawing it to a new context.
     * This ensures the image is in .up orientation regardless of how it was captured.
     * When you draw a UIImage to a graphics context, the orientation is automatically applied.
     * We always normalize to ensure consistent behavior across all devices.
     */
    private fun normalizeImageOrientation(image: UIImage): UIImage {
        // Always normalize by drawing to a new context
        // This ensures the orientation is baked into the pixel data
        val size = image.size
        UIGraphicsBeginImageContextWithOptions(size, false, image.scale)
        
        // Draw the image - this will automatically apply the orientation transformation
        image.drawInRect(CGRectMake(0.0, 0.0, size.useContents { width }, size.useContents { height }))
        
        // Get the normalized image (now with .up orientation baked into pixel data)
        val normalizedImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        
        return normalizedImage ?: image
    }
}
