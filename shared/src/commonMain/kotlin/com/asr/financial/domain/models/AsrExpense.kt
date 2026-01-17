package com.asr.financial.domain.models

import kotlinx.serialization.Serializable

/**
 * ASR Expense model
 */
@Serializable
data class AsrExpense(
    val id: String,
    val type: String,
    val amount: Double,
    val description: String,
    val date: String,
    val category: String
)
