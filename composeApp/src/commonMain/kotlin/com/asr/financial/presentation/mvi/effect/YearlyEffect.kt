package com.asr.financial.presentation.mvi.effect

/**
 * Side effects for Yearly Comparison Screen
 */
sealed class YearlyEffect {
    data class ShowError(val message: String) : YearlyEffect()
}
