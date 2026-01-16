package com.asr.financial.presentation.mvi.state

import com.asr.financial.presentation.screens.calculator.CongregationContribution
import com.asr.financial.presentation.screens.calculator.ContributionCalculation

/**
 * UI State for Calculator Screen
 */
sealed class CalculatorState {
    data object Loading : CalculatorState()
    data class Success(
        val monthlyContribution: ContributionCalculation,
        val yearlyContribution: ContributionCalculation,
        val congregationContributions: List<CongregationContribution>
    ) : CalculatorState()
    data class Error(val message: String) : CalculatorState()
}
