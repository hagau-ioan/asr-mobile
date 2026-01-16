package com.asr.financial.presentation.mvi.effect

sealed class ExpensesEffect {
    data class ShowToast(val message: String) : ExpensesEffect()
}
