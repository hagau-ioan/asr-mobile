package com.asr.financial.data.repository

import com.asr.financial.data.cache.RepositoryCache
import com.asr.financial.data.datasource.DecontDataSource
import com.asr.financial.domain.models.Decont
import com.asr.financial.domain.repository.DecontRepository

/**
 * Implementation of DecontRepository with caching.
 * Uses cache-first strategy: checks cache first, loads from server if cache is empty.
 */
class DecontRepositoryImpl(
    private val dataSource: DecontDataSource
) : DecontRepository {

    private val cache = RepositoryCache<List<Decont>>()

    override suspend fun getAllDeconts(): List<Decont> {
        // Cache-first strategy: check cache first
        val cached = cache.get()
        if (cached != null) {
            return cached
        }

        // Cache is empty, load from server
        val deconts = dataSource.getAll()
        // Cache the result (even if empty) to prevent unnecessary reloads
        cache.set(deconts)
        return deconts
    }

    override suspend fun getDecontByMonth(year: Int, month: Int): Decont? {
        // Use cached data if available
        val allDeconts = getAllDeconts()
        return allDeconts.find { it.year == year && it.month == month }
    }

    override suspend fun refreshData() {
        // Clear cache and force reload from server
        cache.clear()
        val deconts = dataSource.getAll()
        // Cache the result (even if empty) to prevent unnecessary reloads
        cache.set(deconts)
    }
}
