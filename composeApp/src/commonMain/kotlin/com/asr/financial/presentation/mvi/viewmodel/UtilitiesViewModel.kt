package com.asr.financial.presentation.mvi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asr.financial.presentation.mvi.event.UtilitiesEvent
import com.asr.financial.presentation.mvi.interactor.UtilitiesInteractor
import com.asr.financial.presentation.mvi.state.UtilitiesState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UtilitiesViewModel(
    private val interactor: UtilitiesInteractor
) : ViewModel() {

    val uiState: StateFlow<UtilitiesState> = interactor.uiState

    init {
        handleEvent(UtilitiesEvent.LoadData)
    }

    fun handleEvent(event: UtilitiesEvent) {
        viewModelScope.launch {
            interactor.processEvent(event)
        }
    }
}
