package com.asr.financial.presentation.mvi.event

/**
 * UI Events for Calculator Screen
 */
sealed class CalculatorEvent {
    data class LoadData(val year: Int, val month: Int) : CalculatorEvent()
    data class FilterByPeriod(val year: Int, val month: Int) : CalculatorEvent()
}
