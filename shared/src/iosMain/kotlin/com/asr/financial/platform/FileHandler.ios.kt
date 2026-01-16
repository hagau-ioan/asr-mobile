@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package com.asr.financial.platform

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.refTo
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.*
import platform.posix.memcpy

actual class FileHandler {

    private val documentsDirectory: String
        get() {
            val paths = NSFileManager.defaultManager.URLsForDirectory(
                NSDocumentDirectory,
                NSUserDomainMask
            )
            return (paths.firstOrNull() as? NSURL)?.path ?: ""
        }

    actual suspend fun saveFile(fileName: String, data: ByteArray): String = withContext(Dispatchers.IO) {
        val filePath = "$documentsDirectory/$fileName"
        val nsData = data.toNSData()
        nsData.writeToFile(filePath, atomically = true)
        filePath
    }

    actual suspend fun readFile(filePath: String): ByteArray? = withContext(Dispatchers.IO) {
        NSData.dataWithContentsOfFile(filePath)?.toByteArray()
    }

    actual suspend fun deleteFile(filePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            NSFileManager.defaultManager.removeItemAtPath(filePath, null)
            true
        } catch (e: Exception) {
            false
        }
    }

    actual suspend fun fileExists(filePath: String): Boolean = withContext(Dispatchers.IO) {
        NSFileManager.defaultManager.fileExistsAtPath(filePath)
    }

    actual fun getFilePath(fileName: String): String {
        return "$documentsDirectory/$fileName"
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
