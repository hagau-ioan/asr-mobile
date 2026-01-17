package com.asr.financial.presentation.mvi.effect

/**
 * UI Effects for ASR Expenses Screen
 */
sealed interface AsrExpensesEffect {
    data class ShowToast(val message: String) : AsrExpensesEffect
}
