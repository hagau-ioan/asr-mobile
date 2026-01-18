package com.asr.financial.presentation.mvi.effect

/**
 * UI Effects for Login Screen
 */
sealed interface LoginEffect {
    data object NavigateToHome : LoginEffect
    data class ShowToast(val message: String) : LoginEffect
}
