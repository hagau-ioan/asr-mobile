package com.asr.financial.presentation.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetailsUiState(
    val itemId: String = "",
    val title: String = "",
    val description: String = "",
    val isLoading: Boolean = true
)

class DetailsViewModel(
    private val itemId: String
) : ViewModel() {
    private val _uiState = MutableStateFlow(DetailsUiState(itemId = itemId))
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    init {
        loadDetails()
    }

    private fun loadDetails() {
        viewModelScope.launch {
            // Simulate loading
            _uiState.value = DetailsUiState(
                itemId = itemId,
                title = itemId,
                description = "This is the details page for $itemId. " +
                    "Here you would display detailed information about the selected item. " +
                    "This screen demonstrates Navigation 3 with type-safe arguments.",
                isLoading = false
            )
        }
    }
}
