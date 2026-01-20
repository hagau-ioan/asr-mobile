package com.asr.financial.platform

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

actual class FileHandler(private val context: Context) {
    
    private val filesDir = context.filesDir
    
    actual suspend fun saveFile(fileName: String, data: ByteArray): String = withContext(Dispatchers.IO) {
        val file = File(filesDir, fileName)
        file.writeBytes(data)
        file.absolutePath
    }
    
    actual suspend fun readFile(filePath: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            File(filePath).readBytes()
        } catch (e: Exception) {
            null
        }
    }
    
    actual suspend fun deleteFile(filePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            File(filePath).delete()
        } catch (e: Exception) {
            false
        }
    }
    
    actual suspend fun fileExists(filePath: String): Boolean = withContext(Dispatchers.IO) {
        File(filePath).exists()
    }
    
    actual fun getFilePath(fileName: String): String {
        return File(filesDir, fileName).absolutePath
    }
    
    actual suspend fun cleanupTempFiles(prefix: String): Int = withContext(Dispatchers.IO) {
        try {
            val tempDir = File(System.getProperty("java.io.tmpdir"))
            if (!tempDir.exists() || !tempDir.isDirectory) {
                return@withContext 0
            }
            
            var deletedCount = 0
            tempDir.listFiles()?.forEach { file ->
                if (file.isFile && file.name.startsWith(prefix)) {
                    if (file.delete()) {
                        deletedCount++
                    }
                }
            }
            deletedCount
        } catch (e: Exception) {
            0
        }
    }
}
