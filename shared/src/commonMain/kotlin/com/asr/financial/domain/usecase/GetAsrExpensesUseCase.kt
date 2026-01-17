package com.asr.financial.domain.usecase

import com.asr.financial.domain.models.AsrExpense
import com.asr.financial.domain.repository.AsrExpenseRepository

/**
 * UseCase for getting all ASR expenses
 */
class GetAsrExpensesUseCase(
    private val repository: AsrExpenseRepository
) {
    suspend operator fun invoke(): List<AsrExpense> {
        return repository.getAllExpenses()
    }
}
