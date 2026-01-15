package com.asr.financial.domain.models

import kotlinx.datetime.Instant

/**
 * Domain model for Expense
 */
data class Expense(
    val id: Long = 0,
    val description: String,
    val amount: Double,
    val category: ExpenseCategory,
    val month: Int,
    val year: Int,
    val receiptImagePath: String? = null,
    val notes: String? = null,
    val createdAt: Instant
)

enum class ExpenseCategory {
    UTILITIES,
    MAINTENANCE,
    SUPPLIES,
    OTHER
}
