package com.asr.financial.presentation.mvi.effect

/**
 * UI Effects for Expenses Screen
 * One-time events (navigation, toasts, etc.)
 */
sealed interface ExpensesEffect {
    data class ShowToast(val message: String) : ExpensesEffect
}
