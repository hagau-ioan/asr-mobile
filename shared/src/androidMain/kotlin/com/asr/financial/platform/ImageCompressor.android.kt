package com.asr.financial.platform

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

actual class ImageCompressor {
    
    actual suspend fun compress(
        imageData: ByteArray,
        maxWidth: Int,
        quality: Int
    ): ByteArray = withContext(Dispatchers.Default) {
        val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
        
        val scaledBitmap = if (bitmap.width > maxWidth) {
            val ratio = maxWidth.toFloat() / bitmap.width
            val newHeight = (bitmap.height * ratio).toInt()
            Bitmap.createScaledBitmap(bitmap, maxWidth, newHeight, true)
        } else {
            bitmap
        }
        
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        
        if (scaledBitmap != bitmap) {
            scaledBitmap.recycle()
        }
        bitmap.recycle()
        
        outputStream.toByteArray()
    }
}
