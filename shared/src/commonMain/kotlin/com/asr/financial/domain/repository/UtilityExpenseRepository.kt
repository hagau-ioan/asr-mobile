package com.asr.financial.domain.repository

import com.asr.financial.domain.models.FinancialPeriod
import com.asr.financial.domain.models.UtilityExpense
import com.asr.financial.domain.models.UtilityType
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for UtilityExpense operations
 * Implementation in data layer
 */
interface UtilityExpenseRepository {
    fun getAllUtilityExpenses(): Flow<List<UtilityExpense>>
    fun getUtilityExpensesByPeriod(period: FinancialPeriod): Flow<List<UtilityExpense>>
    fun getUtilityExpensesByType(type: UtilityType): Flow<List<UtilityExpense>>
    suspend fun getUtilityExpenseById(id: Long): UtilityExpense?
    suspend fun getTotalByPeriod(period: FinancialPeriod): Double
    suspend fun insertUtilityExpense(utilityExpense: UtilityExpense): Long
    suspend fun updateUtilityExpense(utilityExpense: UtilityExpense)
    suspend fun deleteUtilityExpense(id: Long)
}
