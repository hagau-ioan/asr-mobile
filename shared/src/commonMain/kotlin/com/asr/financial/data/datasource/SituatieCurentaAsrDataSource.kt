package com.asr.financial.data.datasource

import com.asr.financial.domain.models.SituatieCurentaAsr

/**
 * Data source interface for current ASR financial situation
 */
interface SituatieCurentaAsrDataSource {
    suspend fun get(): SituatieCurentaAsr?
}
