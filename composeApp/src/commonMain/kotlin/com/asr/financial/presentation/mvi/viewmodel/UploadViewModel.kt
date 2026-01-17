package com.asr.financial.presentation.mvi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asr.financial.presentation.mvi.effect.UploadEffect
import com.asr.financial.presentation.mvi.event.UploadEvent
import com.asr.financial.presentation.mvi.interactor.UploadInteractor
import com.asr.financial.presentation.mvi.state.UploadState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * ViewModel for Upload Screen
 */
class UploadViewModel(
    private val interactor: UploadInteractor
) : ViewModel() {
    
    val uiState: StateFlow<UploadState> = interactor.uiState
    val uiEffect: Flow<UploadEffect> = interactor.uiEffect

    init {
        handleEvent(UploadEvent.LoadData)
    }

    fun handleEvent(event: UploadEvent) {
        viewModelScope.launch {
            interactor.processEvent(event)
        }
    }
}
