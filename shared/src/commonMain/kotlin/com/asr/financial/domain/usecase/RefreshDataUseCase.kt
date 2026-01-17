package com.asr.financial.domain.usecase

import com.asr.financial.domain.repository.TransactionRepository

/**
 * Use case to refresh all data from remote source.
 * Currently a placeholder for future implementation.
 */
class RefreshDataUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke() {
        repository.refreshData()
    }
}
