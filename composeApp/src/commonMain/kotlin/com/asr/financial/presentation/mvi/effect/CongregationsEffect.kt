package com.asr.financial.presentation.mvi.effect

/**
 * Side effects for Congregations Screen
 */
sealed interface CongregationsEffect {
    data class ShowToast(val message: String) : CongregationsEffect
    data class ShowError(val message: String) : CongregationsEffect
}
