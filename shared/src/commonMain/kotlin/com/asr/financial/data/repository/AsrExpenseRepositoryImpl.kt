package com.asr.financial.data.repository

import com.asr.financial.data.cache.RepositoryCache
import com.asr.financial.data.datasource.AsrExpenseDataSource
import com.asr.financial.domain.models.AsrExpense
import com.asr.financial.domain.repository.AsrExpenseRepository

/**
 * Implementation of AsrExpenseRepository with caching.
 * Uses cache-first strategy: checks cache first, loads from server if cache is empty.
 */
class AsrExpenseRepositoryImpl(
    private val dataSource: AsrExpenseDataSource
) : AsrExpenseRepository {

    private val cache = RepositoryCache<List<AsrExpense>>()

    override suspend fun getAllExpenses(): List<AsrExpense> {
        // Cache-first strategy: check cache first
        val cached = cache.get()
        if (cached != null) {
            return cached
        }

        // Cache is empty, load from server
        val expenses = dataSource.getAll()
        // Cache the result (even if empty) to prevent unnecessary reloads
        cache.set(expenses)
        return expenses
    }

    override suspend fun getExpensesByMonth(month: Int, year: Int): List<AsrExpense> {
        // Use cached data if available
        val allExpenses = getAllExpenses()
        return allExpenses.filter { expense ->
            val parts = expense.date.split("-")
            if (parts.size >= 2) {
                val expenseYear = parts[0].toIntOrNull()
                val expenseMonth = parts[1].toIntOrNull()
                expenseYear == year && expenseMonth == month
            } else {
                false
            }
        }
    }

    override suspend fun getExpensesByCategory(category: String): List<AsrExpense> {
        // Use cached data if available
        val allExpenses = getAllExpenses()
        return allExpenses.filter { it.category == category }
    }

    override suspend fun refreshData() {
        // Clear cache and force reload from server
        cache.clear()
        val expenses = dataSource.getAll()
        // Cache the result (even if empty) to prevent unnecessary reloads
        cache.set(expenses)
    }
}
