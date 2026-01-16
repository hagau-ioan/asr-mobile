package com.asr.financial.presentation.mvi.event

/**
 * Events for Congregations Screen
 */
sealed interface CongregationsEvent {
    data object LoadData : CongregationsEvent
    data class FilterByPeriod(val year: Int, val month: Int) : CongregationsEvent
    data object Refresh : CongregationsEvent
}
