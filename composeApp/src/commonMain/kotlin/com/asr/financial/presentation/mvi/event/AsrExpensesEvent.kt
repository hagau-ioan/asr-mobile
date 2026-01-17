package com.asr.financial.presentation.mvi.event

/**
 * UI Events for ASR Expenses Screen
 */
sealed interface AsrExpensesEvent {
    data object LoadData : AsrExpensesEvent
    data class FilterByPeriod(val year: Int?, val month: Int?) : AsrExpensesEvent
}
