@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package com.asr.financial.platform

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.posix.memcpy

actual class ImageCompressor {

    actual suspend fun compress(
        imageData: ByteArray,
        maxWidth: Int,
        quality: Int
    ): ByteArray = withContext(Dispatchers.IO) {
        val nsData = imageData.toNSData()
        val image = UIImage.imageWithData(nsData) ?: return@withContext imageData

        val currentWidth = image.size.useContents { width }
        val currentHeight = image.size.useContents { height }

        val scaledImage = if (currentWidth > maxWidth) {
            val ratio = maxWidth.toDouble() / currentWidth
            val newHeight = currentHeight * ratio
            val newSize = CGSizeMake(maxWidth.toDouble(), newHeight)

            UIGraphicsBeginImageContextWithOptions(newSize, false, 1.0)
            image.drawInRect(CGRectMake(0.0, 0.0, maxWidth.toDouble(), newHeight))
            val resizedImage = UIGraphicsGetImageFromCurrentImageContext()
            UIGraphicsEndImageContext()

            resizedImage ?: image
        } else {
            image
        }

        val compressedData = UIImageJPEGRepresentation(scaledImage, quality / 100.0)
        compressedData?.toByteArray() ?: imageData
    }
}

private fun ByteArray.toNSData(): NSData {
    if (isEmpty()) return NSData()
    return usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
    }
}

private fun NSData.toByteArray(): ByteArray {
    val length = length.toInt()
    if (length == 0) return ByteArray(0)
    return ByteArray(length).apply {
        usePinned { pinned ->
            memcpy(pinned.addressOf(0), bytes, this@toByteArray.length)
        }
    }
}
