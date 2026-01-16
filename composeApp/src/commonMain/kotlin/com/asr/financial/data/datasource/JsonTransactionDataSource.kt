package com.asr.financial.data.datasource

import com.asr.financial.data.json.JsonResourceLoader
import com.asr.financial.domain.model.Transaction

/**
 * JSON file implementation of TransactionDataSource.
 * Loads data from transactions.json in Compose resources.
 *
 * To switch to a different data source (DB, API), create a new
 * implementation of TransactionDataSource and change the DI binding.
 */
class JsonTransactionDataSource : TransactionDataSource {

    private companion object {
        const val FILE_NAME = "transactions.json"
    }

    override suspend fun getAll(): List<Transaction> {
        val jsonString = JsonResourceLoader.loadJsonString(FILE_NAME) ?: return emptyList()
        return try {
            JsonResourceLoader.json.decodeFromString<List<Transaction>>(jsonString)
        } catch (e: Exception) {
            emptyList()
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
