package com.asr.financial.domain.model

import kotlinx.serialization.Serializable

/**
 * Generic Transaction model for UI display
 * Combines Donations and Expenses
 */
@Serializable
data class Transaction(
    val id: String,
    val type: TransactionType,
    val amount: Double,
    val description: String,
    val date: String,
    val category: String,
    val congregationName: String? = null
)

@Serializable
enum class TransactionType {
    INCOME,
    EXPENSE
}
