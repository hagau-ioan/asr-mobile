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
        val balance: Double,
        // Configuration data - loaded dynamically
        val congregationNames: List<String> = emptyList(),
        val availableYears: List<Int> = emptyList(),
        val expectedDonationPerCongregation: Double = 0.0,
        val totalPublishers: Int = 0,
        val congregationCount: Int = 0
    ) : HomeState

    data class Error(val message: String) : HomeState
}
