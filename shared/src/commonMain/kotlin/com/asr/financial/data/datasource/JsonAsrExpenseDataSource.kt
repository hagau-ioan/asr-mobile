package com.asr.financial.data.datasource

import com.asr.financial.domain.models.AsrExpense
import com.asr.financial.platform.ResourceLoader
import kotlinx.serialization.json.Json

/**
 * JSON implementation of AsrExpenseDataSource
 */
class JsonAsrExpenseDataSource(
    private val resourceLoader: ResourceLoader
) : AsrExpenseDataSource {

    private val json = Json { ignoreUnknownKeys = true }
    private var cachedExpenses: List<AsrExpense>? = null

    private suspend fun loadExpenses(): List<AsrExpense> {
        if (cachedExpenses == null) {
            val jsonString = resourceLoader.loadResourceAsString("asr_expenses_last_12_months.json")
                ?: throw IllegalStateException("Failed to load asr_expenses_last_12_months.json")
            cachedExpenses = json.decodeFromString<List<AsrExpense>>(jsonString)
        }
        return cachedExpenses!!
    }

    override suspend fun getAll(): List<AsrExpense> {
        return loadExpenses()
    }

    override suspend fun getByMonth(month: Int, year: Int): List<AsrExpense> {
        return loadExpenses().filter { expense ->
            val parts = expense.date.split("-")
            val expenseYear = parts[0].toInt()
            val expenseMonth = parts[1].toInt()
            expenseYear == year && expenseMonth == month
        }
    }

    override suspend fun getByCategory(category: String): List<AsrExpense> {
        return loadExpenses().filter { it.category == category }
    }
}
