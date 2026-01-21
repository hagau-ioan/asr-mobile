package com.asr.financial.domain.usecase

import com.asr.financial.domain.models.SituatieCurentaAsr
import com.asr.financial.domain.repository.CurrentAsrSituationRepository

/**
 * UseCase for getting current ASR financial situation
 */
class GetSituatieCurentaAsrUseCase(
    private val repository: CurrentAsrSituationRepository
) {
    suspend operator fun invoke(): SituatieCurentaAsr? {
        return repository.get()
    }
}
