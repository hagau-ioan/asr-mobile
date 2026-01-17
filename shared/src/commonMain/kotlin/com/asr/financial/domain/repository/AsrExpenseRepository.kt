package com.asr.financial.domain.repository

import com.asr.financial.domain.models.AsrExpense

/**
 * Repository interface for ASR expenses
 */
interface AsrExpenseRepository {
    suspend fun getAllExpenses(): List<AsrExpense>
    suspend fun getExpensesByMonth(month: Int, year: Int): List<AsrExpense>
    suspend fun getExpensesByCategory(category: String): List<AsrExpense>
}
