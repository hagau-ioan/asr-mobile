package com.asr.financial.data.repository

import com.asr.financial.data.cache.RepositoryCache
import com.asr.financial.data.datasource.TransactionDataSource
import com.asr.financial.domain.models.Transaction
import com.asr.financial.domain.repository.TransactionRepository

/**
 * Implementation of TransactionRepository with caching.
 * Uses cache-first strategy: checks cache first, loads from server if cache is empty.
 *
 * This class is data-source agnostic - it doesn't know
 * if data comes from JSON, Database, or API.
 */
class TransactionRepositoryImpl(
    private val dataSource: TransactionDataSource
) : TransactionRepository {

    private val cache = RepositoryCache<List<Transaction>>()

    override suspend fun getAllTransactions(): List<Transaction> {
        // Cache-first strategy: check cache first
        val cached = cache.get()
        if (cached != null) {
            return cached
        }

        // Cache is empty, load from server
        val transactions = dataSource.getAll()
        // Cache the result (even if empty) to prevent unnecessary reloads
        cache.set(transactions)
        return transactions
    }

    override suspend fun getTransactionsByMonth(month: Int, year: Int): List<Transaction> {
        // Use cached data if available, otherwise load from server
        val allTransactions = getAllTransactions()
        return allTransactions.filter { transaction ->
            val parts = transaction.date.split("-")
            if (parts.size >= 2) {
                val txnYear = parts[0].toIntOrNull()
                val txnMonth = parts[1].toIntOrNull()
                txnYear == year && txnMonth == month
            } else {
                false
            }
        }
    }

    override suspend fun getTransactionById(id: String): Transaction? {
        // Use cached data if available, otherwise load from server
        val allTransactions = getAllTransactions()
        return allTransactions.firstOrNull { it.id == id }
    }
    
    override suspend fun refreshData() {
        // Clear cache and force reload from server
        cache.clear()
        val transactions = dataSource.getAll()
        // Cache the result (even if empty) to prevent unnecessary reloads
        cache.set(transactions)
    }
}
