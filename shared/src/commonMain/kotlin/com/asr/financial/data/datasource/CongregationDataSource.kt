package com.asr.financial.data.datasource

import com.asr.financial.domain.models.CongregationInfo

/**
 * Data source interface for Congregation data.
 * Implementations can be JSON files, Database, or API.
 */
interface CongregationDataSource {

    /**
     * Get all congregations
     */
    suspend fun getAll(): List<CongregationInfo>

    /**
     * Get only active congregations
     */
    suspend fun getActive(): List<CongregationInfo>

    /**
     * Get congregation by ID
     */
    suspend fun getById(id: String): CongregationInfo?

    /**
     * Get all congregation names
     */
    suspend fun getAllNames(): List<String>

    /**
     * Get total count of congregations
     */
    suspend fun getCount(): Int

    /**
     * Get total member count across all congregations
     */
    suspend fun getTotalMemberCount(): Int
}
