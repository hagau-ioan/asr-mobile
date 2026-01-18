package com.asr.financial.domain.repository

import com.asr.financial.domain.models.CongregationInfo

/**
 * Repository interface for reading Congregation information
 * Uses CongregationInfo model (JSON-friendly, read-only operations)
 */
interface CongregationInfoRepository {
    suspend fun getAll(): List<CongregationInfo>
    suspend fun getActive(): List<CongregationInfo>
    suspend fun getById(id: String): CongregationInfo?
    suspend fun getAllNames(): List<String>
    suspend fun getCount(): Int
    suspend fun getTotalMemberCount(): Int
    
    /**
     * Refresh congregation data from remote source.
     * Clears cache and reloads from server.
     */
    suspend fun refreshData()
}
