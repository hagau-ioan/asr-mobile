package com.asr.financial.presentation.mvi.event

sealed class ExpensesEvent {
    data object LoadData : ExpensesEvent()
    data class FilterByPeriod(val year: Int, val month: Int) : ExpensesEvent()
    data object Refresh : ExpensesEvent()
}
