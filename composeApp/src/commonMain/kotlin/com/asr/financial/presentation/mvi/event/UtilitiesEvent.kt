package com.asr.financial.presentation.mvi.event

/**
 * UI Events for Utilities Screen
 * Represents user actions
 */
sealed interface UtilitiesEvent {
    data object LoadData : UtilitiesEvent
    data class FilterByPeriod(val year: Int, val month: Int) : UtilitiesEvent
    data class ChangeComparisonYear(val year: Int) : UtilitiesEvent
    data object Refresh : UtilitiesEvent
}
