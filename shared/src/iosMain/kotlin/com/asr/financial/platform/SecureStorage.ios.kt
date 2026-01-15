package com.asr.financial.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.*
import platform.Security.*

actual class SecureStorage {
    
    private val serviceName = "com.asr.financial"
    
    actual suspend fun save(key: String, value: String) = withContext(Dispatchers.IO) {
        val query = mutableMapOf<Any?, Any?>().apply {
            put(kSecClass, kSecClassGenericPassword)
            put(kSecAttrService, serviceName)
            put(kSecAttrAccount, key)
            put(kSecValueData, value.encodeToByteArray().toNSData())
        }
        
        SecItemDelete(query as CFDictionaryRef)
        SecItemAdd(query as CFDictionaryRef, null)
    }
    
    actual suspend fun get(key: String): String? = withContext(Dispatchers.IO) {
        val query = mutableMapOf<Any?, Any?>().apply {
            put(kSecClass, kSecClassGenericPassword)
            put(kSecAttrService, serviceName)
            put(kSecAttrAccount, key)
            put(kSecReturnData, kCFBooleanTrue)
            put(kSecMatchLimit, kSecMatchLimitOne)
        }
        
        val result = memScoped {
            val resultPtr = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query as CFDictionaryRef, resultPtr.ptr)
            
            if (status == errSecSuccess) {
                val data = resultPtr.value as? NSData
                data?.toByteArray()?.decodeToString()
            } else {
                null
            }
        }
        
        result
    }
    
    actual suspend fun delete(key: String) = withContext(Dispatchers.IO) {
        val query = mutableMapOf<Any?, Any?>().apply {
            put(kSecClass, kSecClassGenericPassword)
            put(kSecAttrService, serviceName)
            put(kSecAttrAccount, key)
        }
        
        SecItemDelete(query as CFDictionaryRef)
    }
    
    actual suspend fun clear() = withContext(Dispatchers.IO) {
        val query = mutableMapOf<Any?, Any?>().apply {
            put(kSecClass, kSecClassGenericPassword)
            put(kSecAttrService, serviceName)
        }
        
        SecItemDelete(query as CFDictionaryRef)
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
