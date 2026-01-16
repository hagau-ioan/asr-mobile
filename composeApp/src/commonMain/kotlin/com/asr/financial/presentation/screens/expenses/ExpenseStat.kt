package com.asr.financial.presentation.screens.expenses

import com.asr.financial.domain.model.Transaction

/**
 * Presentation model for expense statistics grouped by category
 */
data class ExpenseStat(
    val category: String,
    val amount: Double,
    val count: Int,
    val transactions: List<Transaction>
)
