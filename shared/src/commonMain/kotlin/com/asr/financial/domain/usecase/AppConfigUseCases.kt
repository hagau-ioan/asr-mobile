package com.asr.financial.domain.usecase

import com.asr.financial.domain.models.AppConfig
import com.asr.financial.domain.repository.AppConfigRepository

/**
 * UseCase for getting the full app configuration
 */
class GetAppConfigUseCase(
    private val repository: AppConfigRepository
) {
    suspend operator fun invoke(): AppConfig? {
        return repository.getConfig()
    }
}

/**
 * UseCase for getting available years
 */
class GetAvailableYearsUseCase(
    private val repository: AppConfigRepository
) {
    suspend operator fun invoke(): List<Int> {
        return repository.getAvailableYears()
    }
}

/**
 * UseCase for getting expected donation per congregation
 */
class GetExpectedDonationUseCase(
    private val repository: AppConfigRepository
) {
    suspend operator fun invoke(): Double {
        return repository.getExpectedDonationPerCongregation()
    }
}

/**
 * UseCase for getting total publishers count
 */
class GetTotalPublishersUseCase(
    private val repository: AppConfigRepository
) {
    suspend operator fun invoke(): Int {
        return repository.getTotalPublishers()
    }
}

/**
 * UseCase for getting publisher expected contribution
 */
class GetPublisherExpectedContributionUseCase(
    private val repository: AppConfigRepository
) {
    suspend operator fun invoke(): Double {
        return repository.getConfig()?.financial?.publisherExpectedContribution ?: 30.0
    }
}
