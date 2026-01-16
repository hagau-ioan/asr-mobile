package com.asr.financial.presentation.mvi.state

import com.asr.financial.presentation.screens.home.MissingCongregation

/**
 * UI State for Home Screen
 * Represents the current state of the UI with pre-computed values.
 * All calculations are done in the Interactor, not in the Screen.
 */
sealed interface HomeState {
    data object Loading : HomeState

    data class Success(
        // Selected period
        val selectedYear: Int,
        val selectedMonth: Int,

        // Monthly statistics (for selected period)
        val monthlyIncome: Double,
        val monthlyExpenses: Double,
        val monthlyBalance: Double,

        // Yearly statistics (for selected year)
        val yearlyIncome: Double,
        val yearlyExpenses: Double,
        val yearlyBalance: Double,

        // Missing congregations for selected month
        val missingCongregations: List<MissingCongregation>,

        // Configuration data
        val expectedDonationPerCongregation: Double,
        val totalPublishers: Int,
        val congregationCount: Int,

        // Computed values
        val perPublisherExpense: Double
    ) : HomeState

    data class Error(val message: String) : HomeState
}
