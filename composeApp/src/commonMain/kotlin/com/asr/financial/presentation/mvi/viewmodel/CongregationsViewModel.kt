package com.asr.financial.presentation.mvi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asr.financial.presentation.mvi.event.CongregationsEvent
import com.asr.financial.presentation.mvi.interactor.CongregationsInteractor
import kotlinx.coroutines.launch

/**
 * Congregations ViewModel - Delegates to Interactor
 */
class CongregationsViewModel(
    private val interactor: CongregationsInteractor
) : ViewModel() {

    val uiState = interactor.uiState
    val uiEffect = interactor.uiEffect

    fun handleEvent(event: CongregationsEvent) {
        viewModelScope.launch {
            interactor.processEvent(event)
        }
    }

    init {
        handleEvent(CongregationsEvent.LoadData)
    }
}
