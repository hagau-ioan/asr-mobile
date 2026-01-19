package com.asr.financial.domain.repository

import com.asr.financial.domain.models.Decont

/**
 * Repository interface for Decont (expense reports)
 */
interface DecontRepository {
    /**
     * Get all deconts from backend
     */
    suspend fun getAllDeconts(): List<Decont>

    /**
     * Get decont for a specific month and year
     */
    suspend fun getDecontByMonth(year: Int, month: Int): Decont?

    /**
     * Refresh decont data from remote source.
     * Clears cache and reloads from server.
     */
    suspend fun refreshData()
}
