package com.asr.financial.data.datasource

import com.asr.financial.domain.models.Decont

/**
 * Data source interface for Decont (expense reports)
 */
interface DecontDataSource {
    suspend fun getAll(): List<Decont>
    suspend fun getByMonth(year: Int, month: Int): Decont?
}
