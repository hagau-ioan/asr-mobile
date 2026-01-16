package com.asr.financial.presentation.mvi.effect

/**
 * Side Effects for Calculator Screen
 */
sealed class CalculatorEffect {
    data class ShowError(val message: String) : CalculatorEffect()
}
