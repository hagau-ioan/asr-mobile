package com.asr.financial.domain.repository

import com.asr.financial.domain.models.Transaction

/**
 * Repository for Transaction data
 */
interface TransactionRepository {
    suspend fun getAllTransactions(): List<Transaction>
    suspend fun getTransactionsByMonth(month: Int, year: Int): List<Transaction>
    suspend fun getTransactionById(id: String): Transaction?
    
    /**
     * Refresh data from remote source.
     * Placeholder for future implementation.
     */
    suspend fun refreshData()
}
