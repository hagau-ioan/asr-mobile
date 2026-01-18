package com.asr.financial.data.datasource

import com.asr.financial.domain.models.AsrExpense
import com.asr.financial.platform.FirebaseStorage
import kotlinx.serialization.json.Json

/**
 * Firebase Cloud Storage implementation of AsrExpenseDataSource.
 * Loads data from Firebase Storage: json/asr_expenses_last_12_months.json
 * using platform-specific FirebaseStorage.
 */
class FirebaseStorageAsrExpenseDataSource(
    private val firebaseStorage: FirebaseStorage
) : AsrExpenseDataSource {

    private companion object {
        const val FILE_PATH = "app/asr_expenses_last_12_months.json"
    }

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getAll(): List<AsrExpense> {
        // Load directly from Firebase Storage (caching is handled at repository level)
        val jsonString = firebaseStorage.downloadFileAsString(FILE_PATH)
            ?: throw IllegalStateException("data_error_load_failed")
        return try {
            // Try to decode as wrapped JSON first (Firebase Storage format)
            val wrapped = json.decodeFromString<JsonResponseWrapper<List<AsrExpense>>>(jsonString)
            wrapped.data
        } catch (e: Exception) {
            // Fallback to direct array (for backward compatibility with local files)
            json.decodeFromString<List<AsrExpense>>(jsonString)
        }
    }

    override suspend fun getByMonth(month: Int, year: Int): List<AsrExpense> {
        return getAll().filter { expense ->
            val parts = expense.date.split("-")
            val expenseYear = parts[0].toInt()
            val expenseMonth = parts[1].toInt()
            expenseYear == year && expenseMonth == month
        }
    }

    override suspend fun getByCategory(category: String): List<AsrExpense> {
        return getAll().filter { it.category == category }
    }
}
