package com.asr.financial.data.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Generic in-memory cache for repository data.
 * Thread-safe cache implementation using Mutex for concurrent access.
 *
 * @param T The type of data to cache
 */
class RepositoryCache<T> {
    private var cachedData: T? = null
    private val mutex = Mutex()

    /**
     * Get cached data if available, otherwise null.
     * Thread-safe operation.
     */
    suspend fun get(): T? = mutex.withLock {
        cachedData
    }

    /**
     * Set cached data.
     * Thread-safe operation.
     */
    suspend fun set(data: T) {
        mutex.withLock {
            cachedData = data
        }
    }

    /**
     * Clear the cache.
     * Thread-safe operation.
     */
    suspend fun clear() {
        mutex.withLock {
            cachedData = null
        }
    }

    /**
     * Check if cache has data.
     * Thread-safe operation.
     */
    suspend fun hasData(): Boolean = mutex.withLock {
        cachedData != null
    }
}
