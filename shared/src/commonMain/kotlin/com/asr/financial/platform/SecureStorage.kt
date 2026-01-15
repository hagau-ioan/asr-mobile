package com.asr.financial.platform

/**
 * Platform abstraction for secure storage
 * Android: KeyStore
 * iOS: Keychain
 */
expect class SecureStorage {
    suspend fun save(key: String, value: String)
    suspend fun get(key: String): String?
    suspend fun delete(key: String)
    suspend fun clear()
}
