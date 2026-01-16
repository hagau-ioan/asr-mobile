package com.asr.financial.presentation.mvi.state

import com.asr.financial.presentation.screens.expenses.ExpenseStat

sealed class ExpensesState {
    data object Loading : ExpensesState()
    data class Success(
        val expenses: List<ExpenseStat>,
        val totalExpenses: Double,
        val yearlyTotal: Double,
        val yearlyEndMonth: Int,
        val yearlyEndYear: Int,
        val selectedYear: Int,
        val selectedMonth: Int
    ) : ExpensesState()
    data class Error(val message: String) : ExpensesState()
}
