package com.asr.financial.data.datasource

import com.asr.financial.domain.models.Transaction

/**
 * Data source interface for Transaction data.
 * Implementations can be JSON files, Database, or API.
 *
 * This abstraction allows swapping data sources without
 * modifying the repository or any layer above it.
 */
interface TransactionDataSource {

    /**
     * Get all transactions from the data source
     */
    suspend fun getAll(): List<Transaction>

    /**
     * Get transactions filtered by month and year
     */
    suspend fun getByMonth(month: Int, year: Int): List<Transaction>

    /**
     * Get a single transaction by ID
     */
    suspend fun getById(id: String): Transaction?
}
