package com.asr.financial.domain.repository

import com.asr.financial.domain.model.Transaction

/**
 * Repository for Transaction data
 */
interface TransactionRepository {
    suspend fun getAllTransactions(): List<Transaction>
    suspend fun getTransactionsByMonth(month: Int, year: Int): List<Transaction>
    suspend fun getTransactionById(id: String): Transaction?
}
