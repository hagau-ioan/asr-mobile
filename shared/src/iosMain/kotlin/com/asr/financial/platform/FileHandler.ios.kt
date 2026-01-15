package com.asr.financial.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.*

actual class FileHandler {
    
    private val documentsDirectory: String
        get() = NSFileManager.defaultManager.URLsForDirectory(
            NSDocumentDirectory,
            NSUserDomainMask
        ).first() as? NSURL?.path ?: ""
    
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
    return NSData.create(bytes = this.refTo(0), length = this.size.toULong())
}

private fun NSData.toByteArray(): ByteArray {
    return ByteArray(this.length.toInt()).apply {
        usePinned {
            memcpy(it.addressOf(0), this@toByteArray.bytes, this@toByteArray.length)
        }
    }
}
