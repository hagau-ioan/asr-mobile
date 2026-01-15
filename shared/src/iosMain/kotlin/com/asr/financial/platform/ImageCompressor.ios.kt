package com.asr.financial.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.CoreGraphics.*
import platform.UIKit.*
import platform.Foundation.*

actual class ImageCompressor {
    
    actual suspend fun compress(
        imageData: ByteArray,
        maxWidth: Int,
        quality: Int
    ): ByteArray = withContext(Dispatchers.IO) {
        val nsData = imageData.toNSData()
        val image = UIImage.imageWithData(nsData) ?: return@withContext imageData
        
        val scaledImage = if (image.size.width > maxWidth) {
            val ratio = maxWidth.toDouble() / image.size.width
            val newHeight = image.size.height * ratio
            val newSize = CGSizeMake(maxWidth.toDouble(), newHeight)
            
            UIGraphicsBeginImageContextWithOptions(newSize, false, 1.0)
            image.drawInRect(CGRectMake(0.0, 0.0, newSize.width, newSize.height))
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
    return NSData.create(bytes = this.refTo(0), length = this.size.toULong())
}

private fun NSData.toByteArray(): ByteArray {
    return ByteArray(this.length.toInt()).apply {
        usePinned {
            memcpy(it.addressOf(0), this@toByteArray.bytes, this@toByteArray.length)
        }
    }
}
