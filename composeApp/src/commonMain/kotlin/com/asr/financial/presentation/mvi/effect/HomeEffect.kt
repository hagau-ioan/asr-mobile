package com.asr.financial.presentation.mvi.effect

/**
 * UI Effects for Home Screen
 * One-time events (navigation, toasts, etc.)
 */
sealed interface HomeEffect {
    data class ShowToast(val message: String) : HomeEffect
    data class NavigateToDetails(val transactionId: String) : HomeEffect
    data object ScrollToTop : HomeEffect
}
