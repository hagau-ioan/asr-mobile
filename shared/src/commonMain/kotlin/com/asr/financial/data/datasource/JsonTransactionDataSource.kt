package com.asr.financial.data.datasource

import com.asr.financial.domain.models.Transaction
import com.asr.financial.platform.ResourceLoader
import kotlinx.serialization.json.Json

/**
 * JSON file implementation of TransactionDataSource.
 * Loads data from asr_expenses_transactions.json and cg_donate_transactions.json
 * using platform-specific ResourceLoader.
 */
class JsonTransactionDataSource(
    private val resourceLoader: ResourceLoader
) : TransactionDataSource {

    private companion object {
        const val EXPENSES_FILE = "asr_expenses_transactions.json"
        const val DONATIONS_FILE = "cg_donate_transactions.json"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
        encodeDefaults = true
    }

    override suspend fun getAll(): List<Transaction> {
        val expenses = loadTransactions(EXPENSES_FILE)
        val donations = loadTransactions(DONATIONS_FILE)
        return expenses + donations
    }

    private suspend fun loadTransactions(fileName: String): List<Transaction> {
        val jsonString = resourceLoader.loadResourceAsString(fileName) ?: return emptyList()
        return try {
            // Try to decode as wrapped JSON first (new format)
            val wrapped = json.decodeFromString<JsonResponseWrapper<List<Transaction>>>(jsonString)
            wrapped.data
        } catch (e: Exception) {
            // Fallback to direct array (for backward compatibility)
            try {
                json.decodeFromString<List<Transaction>>(jsonString)
            } catch (e2: Exception) {
                emptyList()
            }
        }
    }

    override suspend fun getByMonth(month: Int, year: Int): List<Transaction> {
        return getAll().filter { transaction ->
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

    override suspend fun getById(id: String): Transaction? {
        return getAll().firstOrNull { it.id == id }
    }
}
