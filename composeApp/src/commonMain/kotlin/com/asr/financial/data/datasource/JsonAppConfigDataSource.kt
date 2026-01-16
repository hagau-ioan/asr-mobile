package com.asr.financial.data.datasource

import com.asr.financial.data.json.JsonResourceLoader
import com.asr.financial.domain.models.AppConfig
import com.asr.financial.platform.Clock
import com.asr.financial.utils.getAvailableYears

/**
 * JSON file implementation of AppConfigDataSource.
 * Loads data from app_config.json in Compose resources.
 */
class JsonAppConfigDataSource(
    private val clock: Clock
) : AppConfigDataSource {

    private companion object {
        const val FILE_NAME = "app_config.json"

        // Default values if config cannot be loaded
        const val DEFAULT_EXPECTED_DONATION = 500.0
        const val DEFAULT_TOTAL_PUBLISHERS = 785
        const val DEFAULT_ORG_NAME = "ASR"
        const val DEFAULT_ORG_LOCATION = "Târgu Mureș"
    }

    private var cachedConfig: AppConfig? = null

    override suspend fun getConfig(): AppConfig? {
        if (cachedConfig != null) return cachedConfig

        val jsonString = JsonResourceLoader.loadJsonString(FILE_NAME) ?: return null
        return try {
            JsonResourceLoader.json.decodeFromString<AppConfig>(jsonString).also {
                cachedConfig = it
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getAvailableYears(): List<Int> {
        return getAvailableYears(clock)
    }

    override suspend fun getExpectedDonationPerCongregation(): Double {
        return getConfig()?.financial?.expectedDonationPerCongregation ?: DEFAULT_EXPECTED_DONATION
    }

    override suspend fun getTotalPublishers(): Int {
        return getConfig()?.financial?.totalPublishers ?: DEFAULT_TOTAL_PUBLISHERS
    }

    override suspend fun getOrganizationName(): String {
        return getConfig()?.organization?.name ?: DEFAULT_ORG_NAME
    }

    override suspend fun getOrganizationLocation(): String {
        return getConfig()?.organization?.location ?: DEFAULT_ORG_LOCATION
    }
}
