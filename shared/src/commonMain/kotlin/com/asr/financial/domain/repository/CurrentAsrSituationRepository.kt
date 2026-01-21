package com.asr.financial.domain.repository

import com.asr.financial.domain.models.SituatieCurentaAsr

/**
 * Repository interface for current ASR financial situation
 */
interface CurrentAsrSituationRepository {
    suspend fun get(): SituatieCurentaAsr?
    
    /**
     * Refresh data from remote source.
     * Clears cache and reloads from server.
     */
    suspend fun refreshData()
}
