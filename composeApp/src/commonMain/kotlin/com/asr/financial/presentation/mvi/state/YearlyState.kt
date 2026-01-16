package com.asr.financial.presentation.mvi.state

import com.asr.financial.presentation.screens.yearly.YearlyComparison
import com.asr.financial.presentation.screens.yearly.YearlyStat

/**
 * UI State for Yearly Comparison Screen
 */
sealed class YearlyState {
    data object Loading : YearlyState()
    
    data class Success(
        val yearlyStats: List<YearlyStat>,
        val comparisons: List<YearlyComparison>
    ) : YearlyState()
    
    data class Error(val message: String) : YearlyState()
}
