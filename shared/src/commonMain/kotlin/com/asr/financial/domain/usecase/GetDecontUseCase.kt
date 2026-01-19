package com.asr.financial.domain.usecase

import com.asr.financial.domain.models.Decont
import com.asr.financial.domain.repository.DecontRepository

/**
 * UseCase for getting decont by month and year
 */
class GetDecontUseCase(
    private val repository: DecontRepository
) {
    suspend operator fun invoke(year: Int, month: Int): Decont? {
        return repository.getDecontByMonth(year, month)
    }
}
