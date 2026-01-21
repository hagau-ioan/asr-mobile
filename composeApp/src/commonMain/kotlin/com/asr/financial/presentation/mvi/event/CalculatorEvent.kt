package com.asr.financial.presentation.mvi.event

/**
 * UI Events for Calculator Screen
 * All calculations are based on last 12 months, no period selection needed
 */
sealed class CalculatorEvent {
    data object LoadData : CalculatorEvent()
    data object FilterByPeriod : CalculatorEvent() // Kept for compatibility, but no longer uses period
}
