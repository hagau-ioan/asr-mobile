package com.asr.financial.presentation.mvi.effect

/**
 * Side Effects for Utilities Screen
 */
sealed interface UtilitiesEffect {
    data class ShowError(val message: String) : UtilitiesEffect
}
