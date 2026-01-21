package com.asr.financial.data.cache

import com.asr.financial.platform.Clock
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Instant

/**
 * Generic in-memory cache for repository data with time-based expiration.
 * Thread-safe cache implementation using Mutex for concurrent access.
 * 
 * @param T The type of data to cache
 * @param cacheDurationMillis Cache expiration duration in milliseconds (default: 12 hours)
 */
class TimeBasedRepositoryCache<T>(
    private val clock: Clock,
    private val cacheDurationMillis: Long = 12 * 60 * 60 * 1000L // 12 hours in milliseconds
) {
    private var cachedData: T? = null
    private var cacheTimestamp: Instant? = null
    private val mutex = Mutex()

    /**
     * Get cached data if available and not expired, otherwise null.
     * Thread-safe operation.
     */
    suspend fun get(): T? = mutex.withLock {
        val cached = cachedData
        val timestamp = cacheTimestamp
        
        if (cached != null && timestamp != null) {
            val now = clock.now()
            val elapsedMillis = (now - timestamp).inWholeMilliseconds
            
            // Check if cache is still valid (not expired)
            if (elapsedMillis < cacheDurationMillis) {
                return cached
            } else {
                // Cache expired, clear it
                cachedData = null
                cacheTimestamp = null
            }
        }
        
        null
    }

    /**
     * Set cached data with current timestamp.
     * Thread-safe operation.
     */
    suspend fun set(data: T) {
        mutex.withLock {
            cachedData = data
            cacheTimestamp = clock.now()
        }
    }

    /**
     * Clear the cache.
     * Thread-safe operation.
     */
    suspend fun clear() {
        mutex.withLock {
            cachedData = null
            cacheTimestamp = null
        }
    }

    /**
     * Check if cache has valid (non-expired) data.
     * Thread-safe operation.
     */
    suspend fun hasData(): Boolean = mutex.withLock {
        val cached = cachedData
        val timestamp = cacheTimestamp
        
        if (cached != null && timestamp != null) {
            val now = clock.now()
            val elapsedMillis = (now - timestamp).inWholeMilliseconds
            return elapsedMillis < cacheDurationMillis
        }
        
        false
    }
}
