package com.asr.financial.data.repository

import com.asr.financial.domain.model.Transaction
import com.asr.financial.domain.repository.TransactionRepository
import kotlinx.serialization.json.Json

/**
 * Mock implementation of TransactionRepository
 * Loads data from JSON files
 */
class MockTransactionRepository(
    private val jsonLoader: JsonLoader
) : TransactionRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override suspend fun getAllTransactions(): List<Transaction> {
        return try {
            val jsonString = jsonLoader.loadJsonFromResources("mock_transactions.json")
            json.decodeFromString<List<Transaction>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getTransactionsByMonth(month: Int, year: Int): List<Transaction> {
        return getAllTransactions().filter { transaction ->
            val parts = transaction.date.split("-")
            if (parts.size == 3) {
                val txnYear = parts[0].toIntOrNull()
                val txnMonth = parts[1].toIntOrNull()
                txnYear == year && txnMonth == month
            } else {
                false
            }
        }
    }

    override suspend fun getTransactionById(id: String): Transaction? {
        return getAllTransactions().firstOrNull { it.id == id }
    }
}

/**
 * Platform-specific JSON loader
 */
interface JsonLoader {
    suspend fun loadJsonFromResources(fileName: String): String
}
