package com.asr.financial.domain.repository

import com.asr.financial.domain.models.Expense
import com.asr.financial.domain.models.ExpenseCategory
import com.asr.financial.domain.models.FinancialPeriod
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Expense operations
 * Implementation in data layer
 */
interface ExpenseRepository {
    fun getAllExpenses(): Flow<List<Expense>>
    fun getExpensesByPeriod(period: FinancialPeriod): Flow<List<Expense>>
    fun getExpensesByCategory(category: ExpenseCategory): Flow<List<Expense>>
    suspend fun getExpenseById(id: Long): Expense?
    suspend fun getTotalByPeriod(period: FinancialPeriod): Double
    suspend fun insertExpense(expense: Expense): Long
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(id: Long)
}
