package com.asr.financial.presentation.mvi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asr.financial.presentation.mvi.event.AsrExpensesEvent
import com.asr.financial.presentation.mvi.interactor.AsrExpensesInteractor
import kotlinx.coroutines.launch

/**
 * ViewModel for ASR Expenses Screen
 */
class AsrExpensesViewModel(
    private val interactor: AsrExpensesInteractor
) : ViewModel() {

    val uiState = interactor.uiState
    val uiEffect = interactor.uiEffect

    init {
        handleEvent(AsrExpensesEvent.LoadData)
    }

    fun handleEvent(event: AsrExpensesEvent) {
        viewModelScope.launch {
            interactor.processEvent(event)
        }
    }
}
