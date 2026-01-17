package com.asr.financial.data.repository

import com.asr.financial.data.datasource.AsrExpenseDataSource
import com.asr.financial.domain.models.AsrExpense
import com.asr.financial.domain.repository.AsrExpenseRepository

/**
 * Implementation of AsrExpenseRepository
 */
class AsrExpenseRepositoryImpl(
    private val dataSource: AsrExpenseDataSource
) : AsrExpenseRepository {

    override suspend fun getAllExpenses(): List<AsrExpense> {
        return dataSource.getAll()
    }

    override suspend fun getExpensesByMonth(month: Int, year: Int): List<AsrExpense> {
        return dataSource.getByMonth(month, year)
    }

    override suspend fun getExpensesByCategory(category: String): List<AsrExpense> {
        return dataSource.getByCategory(category)
    }
}
