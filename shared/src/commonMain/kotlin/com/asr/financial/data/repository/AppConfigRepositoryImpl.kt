package com.asr.financial.data.repository

import com.asr.financial.data.datasource.AppConfigDataSource
import com.asr.financial.domain.models.AppConfig
import com.asr.financial.domain.repository.AppConfigRepository

/**
 * Implementation of AppConfigRepository.
 * Delegates to the injected DataSource.
 */
class AppConfigRepositoryImpl(
    private val dataSource: AppConfigDataSource
) : AppConfigRepository {

    override suspend fun getConfig(): AppConfig? {
        return dataSource.getConfig()
    }

    override suspend fun getAvailableYears(): List<Int> {
        return dataSource.getAvailableYears()
    }

    override suspend fun getExpectedDonationPerCongregation(): Double {
        return dataSource.getExpectedDonationPerCongregation()
    }

    override suspend fun getTotalPublishers(): Int {
        return dataSource.getTotalPublishers()
    }

    override suspend fun getOrganizationName(): String {
        return dataSource.getOrganizationName()
    }

    override suspend fun getOrganizationLocation(): String {
        return dataSource.getOrganizationLocation()
    }
}
