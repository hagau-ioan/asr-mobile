package com.asr.financial.presentation.mvi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asr.financial.presentation.mvi.effect.CalculatorEffect
import com.asr.financial.presentation.mvi.event.CalculatorEvent
import com.asr.financial.presentation.mvi.interactor.CalculatorInteractor
import com.asr.financial.presentation.mvi.state.CalculatorState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Calculator Screen
 */
class CalculatorViewModel(
    private val interactor: CalculatorInteractor
) : ViewModel() {

    val uiState: StateFlow<CalculatorState> = interactor.uiState as StateFlow<CalculatorState>
    val effect: Flow<CalculatorEffect> = interactor.effect

    fun handleEvent(event: CalculatorEvent) {
        viewModelScope.launch {
            interactor.handleEvent(event)
        }
    }
}
