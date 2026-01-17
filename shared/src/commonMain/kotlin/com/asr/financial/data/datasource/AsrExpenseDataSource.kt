package com.asr.financial.data.datasource

import com.asr.financial.domain.models.AsrExpense

/**
 * Data source interface for ASR expenses
 */
interface AsrExpenseDataSource {
    suspend fun getAll(): List<AsrExpense>
    suspend fun getByMonth(month: Int, year: Int): List<AsrExpense>
    suspend fun getByCategory(category: String): List<AsrExpense>
}
