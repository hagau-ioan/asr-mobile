package com.asr.financial.data.repository

import com.asr.financial.data.datasource.TransactionDataSource
import com.asr.financial.domain.model.Transaction
import com.asr.financial.domain.repository.TransactionRepository

/**
 * Implementation of TransactionRepository.
 * Delegates to the injected DataSource.
 *
 * This class is data-source agnostic - it doesn't know
 * if data comes from JSON, Database, or API.
 */
class TransactionRepositoryImpl(
    private val dataSource: TransactionDataSource
) : TransactionRepository {

    override suspend fun getAllTransactions(): List<Transaction> {
        return dataSource.getAll()
    }

    override suspend fun getTransactionsByMonth(month: Int, year: Int): List<Transaction> {
        return dataSource.getByMonth(month, year)
    }

    override suspend fun getTransactionById(id: String): Transaction? {
        return dataSource.getById(id)
    }
    
    override suspend fun refreshData() {
        // Placeholder for future remote data refresh
        // Will implement API call here
        kotlinx.coroutines.delay(2000) // Simulate network delay
    }
}
