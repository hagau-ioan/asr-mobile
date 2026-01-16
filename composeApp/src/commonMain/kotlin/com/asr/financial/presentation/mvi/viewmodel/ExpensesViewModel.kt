package com.asr.financial.presentation.mvi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asr.financial.presentation.mvi.event.ExpensesEvent
import com.asr.financial.presentation.mvi.interactor.ExpensesInteractor
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExpensesViewModel(
    private val interactor: ExpensesInteractor
) : ViewModel() {

    val uiState: StateFlow<com.asr.financial.presentation.mvi.state.ExpensesState> = interactor.uiState

    init {
        handleEvent(ExpensesEvent.LoadData)
    }

    fun handleEvent(event: ExpensesEvent) {
        viewModelScope.launch {
            interactor.processEvent(event)
        }
    }
}
