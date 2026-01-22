package com.asr.financial.presentation.mvi.event

/**
 * UI Events for Home Screen
 * Represents user actions
 */
sealed interface HomeEvent {
    data object LoadData : HomeEvent
    data class FilterByMonth(val month: Int, val year: Int) : HomeEvent
    data class NavigateToDetails(val transactionId: String) : HomeEvent
    data object Refresh : HomeEvent
    data object DismissNotification : HomeEvent
}
