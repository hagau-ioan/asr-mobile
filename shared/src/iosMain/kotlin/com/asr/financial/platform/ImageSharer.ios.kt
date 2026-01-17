package com.asr.financial.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

/**
 * iOS implementation of ImageSharer.
 * Uses UIActivityViewController for system share sheet.
 */
@OptIn(ExperimentalForeignApi::class)
actual class ImageSharer {
    actual suspend fun shareImage(imageData: ByteArray, fileName: String, title: String): Boolean {
        return try {
            val tempDir = NSTemporaryDirectory()
            val filePath = "${tempDir}$fileName"

            val nsData = imageData.usePinned { pinned ->
                NSData.create(bytes = pinned.addressOf(0), length = imageData.size.toULong())
            }
            nsData.writeToFile(filePath, atomically = true)

            val fileUrl = NSURL.fileURLWithPath(filePath)

            withContext(Dispatchers.Main) {
                val activityViewController = UIActivityViewController(
                    activityItems = listOf(fileUrl),
                    applicationActivities = null
                )

                val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
                rootViewController?.presentViewController(
                    activityViewController,
                    animated = true,
                    completion = null
                )
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
