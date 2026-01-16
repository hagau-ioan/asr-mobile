package com.asr.financial.presentation.mvi.state

import com.asr.financial.domain.model.Transaction

/**
 * UI State for Home Screen
 * Represents the current state of the UI
 */
sealed interface HomeState {
    data object Loading : HomeState
    
    data class Success(
        val transactions: List<Transaction>,
        val totalIncome: Double,
        val totalExpenses: Double,
        val balance: Double
    ) : HomeState
    
    data class Error(val message: String) : HomeState
}
