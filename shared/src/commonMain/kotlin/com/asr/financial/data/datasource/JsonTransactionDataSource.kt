package com.asr.financial.data.datasource

import com.asr.financial.domain.model.Transaction
import com.asr.financial.platform.ResourceLoader
import kotlinx.serialization.json.Json

/**
 * JSON file implementation of TransactionDataSource.
 * Loads data from transactions.json using platform-specific ResourceLoader.
 */
class JsonTransactionDataSource(
    private val resourceLoader: ResourceLoader
) : TransactionDataSource {

    private companion object {
        const val FILE_NAME = "transactions.json"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
        encodeDefaults = true
    }

    override suspend fun getAll(): List<Transaction> {
        val jsonString = resourceLoader.loadResourceAsString(FILE_NAME) ?: return emptyList()
        return try {
            json.decodeFromString<List<Transaction>>(jsonString)
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
