package com.asr.financial.data.repository

import com.asr.financial.data.cache.RepositoryCache
import com.asr.financial.data.datasource.CongregationDataSource
import com.asr.financial.domain.models.CongregationInfo
import com.asr.financial.domain.repository.CongregationInfoRepository

/**
 * Implementation of CongregationInfoRepository with caching.
 * Uses cache-first strategy: checks cache first, loads from server if cache is empty.
 */
class CongregationInfoRepositoryImpl(
    private val dataSource: CongregationDataSource
) : CongregationInfoRepository {

    private val cache = RepositoryCache<List<CongregationInfo>>()

    override suspend fun getAll(): List<CongregationInfo> {
        // Cache-first strategy: check cache first
        val cached = cache.get()
        if (cached != null) {
            return cached
        }

        // Cache is empty, load from server
        val congregations = dataSource.getAll()
        // Cache the result (even if empty) to prevent unnecessary reloads
        cache.set(congregations)
        return congregations
    }

    override suspend fun getActive(): List<CongregationInfo> {
        // Use cached data if available
        val all = getAll()
        return all.filter { it.isActive }
    }

    override suspend fun getById(id: String): CongregationInfo? {
        // Use cached data if available
        val all = getAll()
        return all.firstOrNull { it.id == id }
    }

    override suspend fun getAllNames(): List<String> {
        // Use cached data if available
        val all = getAll()
        return all.map { it.name }
    }

    override suspend fun getCount(): Int {
        // Use cached data if available
        val all = getAll()
        return all.size
    }

    override suspend fun getTotalMemberCount(): Int {
        // Use cached data if available
        val all = getAll()
        return all.sumOf { it.memberCount }
    }

    override suspend fun refreshData() {
        // Clear cache and force reload from server
        cache.clear()
        val congregations = dataSource.getAll()
        // Cache the result (even if empty) to prevent unnecessary reloads
        cache.set(congregations)
    }
}
