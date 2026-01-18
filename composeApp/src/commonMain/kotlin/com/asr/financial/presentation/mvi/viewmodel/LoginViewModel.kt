package com.asr.financial.presentation.mvi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asr.financial.presentation.mvi.event.LoginEvent
import com.asr.financial.presentation.mvi.interactor.LoginInteractor
import kotlinx.coroutines.launch

/**
 * Login ViewModel - Delegates to Interactor
 * Follows MVI pattern consistent with other ViewModels
 */
class LoginViewModel(
    private val interactor: LoginInteractor
) : ViewModel() {
    
    val uiState = interactor.uiState
    val uiEffect = interactor.uiEffect
    
    fun handleEvent(event: LoginEvent) {
        viewModelScope.launch {
            interactor.processEvent(event)
        }
    }
}
