package com.asr.financial.domain.repository

import com.asr.financial.domain.models.AppConfig

/**
 * Repository interface for Application Configuration
 */
interface AppConfigRepository {
    suspend fun getConfig(): AppConfig?
    suspend fun getAvailableYears(): List<Int>
    suspend fun getExpectedDonationPerCongregation(): Double
    suspend fun getTotalPublishers(): Int
    suspend fun getOrganizationName(): String
    suspend fun getOrganizationLocation(): String
    
    /**
     * Refresh configuration data from remote source.
     * Clears cache and reloads from server.
     */
    suspend fun refreshData()
}
