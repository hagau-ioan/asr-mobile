package com.asr.financial.domain.usecase

import com.asr.financial.domain.repository.AppConfigRepository

/**
 * Get start year from configuration
 */
class GetStartYearUseCase(
    private val repository: AppConfigRepository
) {
    suspend operator fun invoke(): Int {
        return repository.getConfig()?.financial?.startYear ?: 2024
    }
}
