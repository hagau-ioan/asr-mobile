package com.asr.financial.data.repository

import com.asr.financial.data.cache.RepositoryCache
import com.asr.financial.data.datasource.AppConfigDataSource
import com.asr.financial.domain.models.AppConfig
import com.asr.financial.domain.repository.AppConfigRepository

/**
 * Implementation of AppConfigRepository with caching.
 * Uses cache-first strategy: checks cache first, loads from server if cache is empty.
 */
class AppConfigRepositoryImpl(
    private val dataSource: AppConfigDataSource
) : AppConfigRepository {

    private val cache = RepositoryCache<AppConfig>()

    override suspend fun getConfig(): AppConfig? {
        // Cache-first strategy: check cache first
        val cached = cache.get()
        if (cached != null) {
            return cached
        }

        // Cache is empty, load from server
        val config = dataSource.getConfig()
        if (config != null) {
            cache.set(config)
        }
        return config
    }

    override suspend fun getAvailableYears(): List<Int> {
        // getAvailableYears() is calculated from clock, doesn't need config cache
        // but we ensure config is loaded first to populate cache if needed
        getConfig()
        return dataSource.getAvailableYears()
    }

    override suspend fun getExpectedDonationPerCongregation(): Double {
        // Use cached config - getConfig() will use cache or load from server once
        val config = getConfig()
        return config?.financial?.expectedDonationPerCongregation
            ?: throw IllegalStateException("config_error_load_failed")
    }

    override suspend fun getTotalPublishers(): Int {
        // Use cached config - getConfig() will use cache or load from server once
        val config = getConfig()
        return config?.financial?.totalPublishers
            ?: throw IllegalStateException("config_error_load_failed")
    }

    override suspend fun getOrganizationName(): String {
        // Use cached config - getConfig() will use cache or load from server once
        val config = getConfig()
        return config?.organization?.name
            ?: throw IllegalStateException("config_error_load_failed")
    }

    override suspend fun getOrganizationLocation(): String {
        // Use cached config - getConfig() will use cache or load from server once
        val config = getConfig()
        return config?.organization?.location
            ?: throw IllegalStateException("config_error_load_failed")
    }

    override suspend fun refreshData() {
        // Clear cache and force reload from server
        cache.clear()
        val config = dataSource.getConfig()
        if (config != null) {
            cache.set(config)
        }
    }
}
