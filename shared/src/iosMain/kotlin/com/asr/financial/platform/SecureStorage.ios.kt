@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package com.asr.financial.platform

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.*
import platform.Security.*
import platform.posix.memcpy

actual class SecureStorage {

    private val serviceName = "com.asr.financial"

    actual suspend fun save(key: String, value: String): Unit = withContext(Dispatchers.IO) {
        val query = mutableMapOf<Any?, Any?>().apply {
            put(kSecClass, kSecClassGenericPassword)
            put(kSecAttrService, serviceName)
            put(kSecAttrAccount, key)
            put(kSecValueData, value.encodeToByteArray().toNSData())
        }

        SecItemDelete(query as CFDictionaryRef)
        SecItemAdd(query as CFDictionaryRef, null)
        Unit
    }

    actual suspend fun get(key: String): String? = withContext(Dispatchers.IO) {
        val query = mutableMapOf<Any?, Any?>().apply {
            put(kSecClass, kSecClassGenericPassword)
            put(kSecAttrService, serviceName)
            put(kSecAttrAccount, key)
            put(kSecReturnData, kCFBooleanTrue)
            put(kSecMatchLimit, kSecMatchLimitOne)
        }

        memScoped {
            val resultPtr = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query as CFDictionaryRef, resultPtr.ptr)

            if (status == errSecSuccess) {
                val data = resultPtr.value as? NSData
                data?.toByteArray()?.decodeToString()
            } else {
                null
            }
        }
    }

    actual suspend fun delete(key: String): Unit = withContext(Dispatchers.IO) {
        val query = mutableMapOf<Any?, Any?>().apply {
            put(kSecClass, kSecClassGenericPassword)
            put(kSecAttrService, serviceName)
            put(kSecAttrAccount, key)
        }

        SecItemDelete(query as CFDictionaryRef)
        Unit
    }

    actual suspend fun clear(): Unit = withContext(Dispatchers.IO) {
        val query = mutableMapOf<Any?, Any?>().apply {
            put(kSecClass, kSecClassGenericPassword)
            put(kSecAttrService, serviceName)
        }

        SecItemDelete(query as CFDictionaryRef)
        Unit
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
