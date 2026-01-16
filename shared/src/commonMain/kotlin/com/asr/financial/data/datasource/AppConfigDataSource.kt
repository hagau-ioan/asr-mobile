package com.asr.financial.data.datasource

import com.asr.financial.domain.models.AppConfig

/**
 * Data source interface for Application Configuration.
 * Implementations can be JSON files, Database, or API.
 */
interface AppConfigDataSource {

    /**
     * Get the full application configuration
     */
    suspend fun getConfig(): AppConfig?

    /**
     * Get available years for selection
     */
    suspend fun getAvailableYears(): List<Int>

    /**
     * Get expected donation per congregation
     */
    suspend fun getExpectedDonationPerCongregation(): Double

    /**
     * Get total publisher count
     */
    suspend fun getTotalPublishers(): Int

    /**
     * Get organization name
     */
    suspend fun getOrganizationName(): String

    /**
     * Get organization location
     */
    suspend fun getOrganizationLocation(): String
}
