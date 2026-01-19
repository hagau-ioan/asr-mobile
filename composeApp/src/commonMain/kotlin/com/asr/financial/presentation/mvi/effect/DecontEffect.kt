package com.asr.financial.presentation.mvi.effect

/**
 * UI Effects for Decont Screen
 */
sealed interface DecontEffect {
    data class ShowToast(val message: String) : DecontEffect
}
