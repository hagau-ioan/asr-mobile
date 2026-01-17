package com.asr.financial.presentation.mvi.state

import com.asr.financial.domain.models.AsrExpense

/**
 * UI State for ASR Expenses Screen
 */
sealed interface AsrExpensesState {
    data object Loading : AsrExpensesState
    data class Success(
        val expenses: List<AsrExpense>,
        val totalAmount: Double,
        val selectedYear: Int,
        val selectedMonth: Int,
        val availableYears: List<Int>
    ) : AsrExpensesState
    data class Error(val message: String) : AsrExpensesState
}
