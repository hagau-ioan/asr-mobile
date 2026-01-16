package com.asr.financial.presentation.mvi.event

/**
 * UI Events for Expenses Screen
 * Represents user actions
 */
sealed interface ExpensesEvent {
    data object LoadData : ExpensesEvent
    data class FilterByPeriod(val year: Int, val month: Int) : ExpensesEvent
    data object Refresh : ExpensesEvent
}
