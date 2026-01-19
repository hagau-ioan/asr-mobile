package com.asr.financial.presentation.mvi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asr.financial.presentation.mvi.event.DecontEvent
import com.asr.financial.presentation.mvi.interactor.DecontInteractor
import kotlinx.coroutines.launch

/**
 * ViewModel for Decont Screen
 */
class DecontViewModel(
    private val interactor: DecontInteractor
) : ViewModel() {

    val uiState = interactor.uiState
    val uiEffect = interactor.uiEffect

    fun handleEvent(event: DecontEvent) {
        viewModelScope.launch {
            interactor.processEvent(event)
        }
    }
}
