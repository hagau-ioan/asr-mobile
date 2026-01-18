package com.asr.financial.data.datasource

import com.asr.financial.domain.models.AppConfig
import com.asr.financial.platform.Clock
import com.asr.financial.platform.FirebaseStorage
import com.asr.financial.utils.getAvailableYears
import kotlinx.serialization.json.Json

/**
 * Firebase Cloud Storage implementation of AppConfigDataSource.
 * Loads data from Firebase Storage: app/app_config.json
 * using platform-specific FirebaseStorage.
 */
class FirebaseStorageAppConfigDataSource(
    private val clock: Clock,
    private val firebaseStorage: FirebaseStorage
) : AppConfigDataSource {

    private companion object {
        const val FILE_PATH = "app/app_config.json"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
        encodeDefaults = true
    }

    override suspend fun getConfig(): AppConfig? {
        // Load directly from Firebase Storage (caching is handled at repository level)
        val jsonString = firebaseStorage.downloadFileAsString(FILE_PATH) ?: return null
        return try {
            // Try to decode as wrapped JSON first (Firebase Storage format)
            val wrapped = json.decodeFromString<JsonResponseWrapper<AppConfig>>(jsonString)
            wrapped.data
        } catch (e: Exception) {
            // Fallback to direct object (for backward compatibility with local files)
            try {
                json.decodeFromString<AppConfig>(jsonString)
            } catch (e2: Exception) {
                null
            }
        }
    }

    override suspend fun getAvailableYears(): List<Int> {
        return getAvailableYears(clock)
    }

    override suspend fun getExpectedDonationPerCongregation(): Double {
        return getConfig()?.financial?.expectedDonationPerCongregation
            ?: throw IllegalStateException("config_error_load_failed")
    }

    override suspend fun getTotalPublishers(): Int {
        return getConfig()?.financial?.totalPublishers
            ?: throw IllegalStateException("config_error_load_failed")
    }

    override suspend fun getOrganizationName(): String {
        return getConfig()?.organization?.name
            ?: throw IllegalStateException("config_error_load_failed")
    }

    override suspend fun getOrganizationLocation(): String {
        return getConfig()?.organization?.location
            ?: throw IllegalStateException("config_error_load_failed")
    }
}
