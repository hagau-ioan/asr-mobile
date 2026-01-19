package com.asr.financial.presentation.mvi.interactor

import com.asr.financial.domain.usecase.LoginUseCase
import com.asr.financial.presentation.mvi.effect.LoginEffect
import com.asr.financial.presentation.mvi.event.LoginEvent
import com.asr.financial.presentation.mvi.interactor.LoginMessages
import com.asr.financial.presentation.mvi.state.LoginState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Interactor for Login Screen
 * Handles login business logic and state management
 * Follows MVI pattern consistent with other Interactors
 */
class LoginInteractor(
    private val loginUseCase: LoginUseCase
) {
    private val _uiState = MutableStateFlow<LoginState>(LoginState.Ready())
    val uiState = _uiState.asStateFlow()
    
    private val _uiEffectChannel = Channel<LoginEffect>(Channel.BUFFERED)
    val uiEffect: Flow<LoginEffect> = _uiEffectChannel.receiveAsFlow()
    
    suspend fun processEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> updateEmail(event.email)
            is LoginEvent.PasswordChanged -> updatePassword(event.password)
            is LoginEvent.Login -> performLogin()
            is LoginEvent.ClearError -> clearError()
        }
    }
    
    private suspend fun updateEmail(email: String) {
        val currentState = _uiState.value as? LoginState.Ready ?: LoginState.Ready()
        _uiState.emit(currentState.copy(email = email, errorMessage = null))
    }
    
    private suspend fun updatePassword(password: String) {
        val currentState = _uiState.value as? LoginState.Ready ?: LoginState.Ready()
        _uiState.emit(currentState.copy(password = password, errorMessage = null))
    }
    
    private suspend fun clearError() {
        val currentState = _uiState.value as? LoginState.Ready ?: LoginState.Ready()
        _uiState.emit(currentState.copy(errorMessage = null))
    }
    
    private suspend fun performLogin() {
        val currentState = _uiState.value as? LoginState.Ready ?: LoginState.Ready()
        
        // Validate input
        if (currentState.email.isBlank()) {
            _uiState.emit(currentState.copy(errorMessage = LoginMessages.ERROR_EMPTY_EMAIL))
            return
        }
        
        if (currentState.password.isBlank()) {
            _uiState.emit(currentState.copy(errorMessage = LoginMessages.ERROR_EMPTY_PASSWORD))
            return
        }
        
        // Validate email format (basic check)
        if (!isValidEmail(currentState.email)) {
            _uiState.emit(currentState.copy(errorMessage = LoginMessages.ERROR_INVALID_EMAIL))
            return
        }
        
        // Show loading state
        _uiState.emit(currentState.copy(isLoading = true, errorMessage = null))
        
        try {
            val result = loginUseCase(currentState.email, currentState.password)
            
            result.fold(
                onSuccess = { user ->
                    // Login successful - navigate to home
                    _uiState.emit(currentState.copy(isLoading = false))
                    _uiEffectChannel.send(LoginEffect.NavigateToHome)
                },
                onFailure = { exception ->
                    // Login failed - show error
                    val errorMessage = exception.message?.takeIf { it.startsWith("login_error_") } 
                        ?: LoginMessages.ERROR_UNKNOWN
                    _uiState.emit(currentState.copy(isLoading = false, errorMessage = errorMessage))
                    // Toast effect removed - errors are shown in UI via errorMessage
                }
            )
        } catch (e: Exception) {
            _uiState.emit(
                currentState.copy(
                    isLoading = false,
                    errorMessage = e.message?.takeIf { it.startsWith("login_error_") } 
                        ?: LoginMessages.ERROR_UNKNOWN
                )
            )
            // Toast effect removed - errors are shown in UI via errorMessage
        }
    }
    
    /**
     * Basic email validation
     */
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()
        return emailRegex.matches(email)
    }
}
