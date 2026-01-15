package com.asr.financial.domain.repository

import com.asr.financial.domain.models.Congregation
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Congregation operations
 * Implementation in data layer
 */
interface CongregationRepository {
    fun getAllCongregations(): Flow<List<Congregation>>
    fun getActiveCongregations(): Flow<List<Congregation>>
    suspend fun getCongregationById(id: Long): Congregation?
    suspend fun insertCongregation(congregation: Congregation): Long
    suspend fun updateCongregation(congregation: Congregation)
    suspend fun deleteCongregation(id: Long)
}
