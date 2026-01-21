package com.asr.financial.data.repository

import com.asr.financial.data.cache.RepositoryCache
import com.asr.financial.data.datasource.DecontDataSource
import com.asr.financial.domain.models.Decont
import com.asr.financial.domain.repository.DecontRepository

/**
 * Implementation of DecontRepository with caching.
 * Uses cache-first strategy: checks cache first, loads from server if cache is empty.
 */
class DecontRepositoryImpl(
    private val dataSource: DecontDataSource
) : DecontRepository {

    private val cache = RepositoryCache<List<Decont>>()

    override suspend fun getAllDeconts(): List<Decont> {
        // Cache-first strategy: check cache first
        val cached = cache.get()
        if (cached != null) {
            return cached
        }

        // Cache is empty, load from server
        val deconts = dataSource.getAll()
        // Cache the result (even if empty) to prevent unnecessary reloads
        cache.set(deconts)
        return deconts
    }

    override suspend fun getDecontByMonth(year: Int, month: Int): Decont? {
        // Use cached data if available
        val allDeconts = getAllDeconts()
        
        // First, try exact match by decont's year/month
        val exactMatch = allDeconts.find { it.year == year && it.month == month }
        if (exactMatch != null) {
            return exactMatch
        }
        
        // Fallback: Search by expense dates (handles cases where backend groups expenses incorrectly)
        // Look for decont where most expenses are from the requested month/year
        return allDeconts.firstOrNull { decont ->
            val matchingExpenses = decont.expenses.count { expense ->
                try {
                    // Parse date format: "DD.MM.YYYY" (e.g., "04.11.2025")
                    val parts = expense.date.split(".")
                    if (parts.size == 3) {
                        val expenseDay = parts[0].toIntOrNull()
                        val expenseMonth = parts[1].toIntOrNull()
                        val expenseYear = parts[2].toIntOrNull()
                        expenseYear == year && expenseMonth == month
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    false
                }
            }
            // Return decont if at least one expense matches the requested month/year
            matchingExpenses > 0
        }
    }

    override suspend fun refreshData() {
        // Clear cache and force reload from server
        cache.clear()
        val deconts = dataSource.getAll()
        // Cache the result (even if empty) to prevent unnecessary reloads
        cache.set(deconts)
    }
}
