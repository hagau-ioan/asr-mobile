package com.asr.financial.data.datasource

import com.asr.financial.domain.models.Transaction
import com.asr.financial.platform.FirebaseStorage
import kotlinx.serialization.json.Json

/**
 * Firebase Cloud Storage implementation of TransactionDataSource.
 * Loads data from Firebase Storage: app/asr_expenses_transactions.json and app/cg_donate_transactions.json
 * using platform-specific FirebaseStorage.
 */
class FirebaseStorageTransactionDataSource(
    private val firebaseStorage: FirebaseStorage
) : TransactionDataSource {

    private companion object {
        const val EXPENSES_FILE = "app/asr_expenses_transactions.json"
        const val DONATIONS_FILE = "app/cg_donate_transactions.json"
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

    private suspend fun loadTransactions(filePath: String): List<Transaction> {
        val jsonString = firebaseStorage.downloadFileAsString(filePath) ?: return emptyList()
        return try {
            // Try to decode as wrapped JSON first (Firebase Storage format)
            val wrapped = json.decodeFromString<JsonResponseWrapper<List<Transaction>>>(jsonString)
            wrapped.data
        } catch (e: Exception) {
            // Fallback to direct array (for backward compatibility with local files)
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
