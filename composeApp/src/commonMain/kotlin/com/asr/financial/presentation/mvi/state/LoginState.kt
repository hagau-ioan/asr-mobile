package com.asr.financial.presentation.mvi.state

/**
 * UI State for Login Screen
 * 
 * Note: Login screen uses Ready state with isLoading flag instead of separate Loading state
 * because the form is always visible and we just show loading indicator during login attempt.
 */
sealed interface LoginState {
    /**
     * Ready state - form is ready for input
     * isLoading: true when login is in progress
     * errorMessage: null when no error, or error key string when error occurs
     */
    data class Ready(
        val email: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    ) : LoginState
}
