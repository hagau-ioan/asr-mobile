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
    val date: String, // Format: YYYY-MM-DD
    val category: String,
    val congregationName: String? = null
) {
    /**
     * Extract year from date string (YYYY-MM-DD)
     */
    fun getYear(): Int = date.substring(0, 4).toInt()
    
    /**
     * Extract month from date string (YYYY-MM-DD)
     * Returns 1-12
     */
    fun getMonth(): Int = date.substring(5, 7).toInt()
    
    /**
     * Get month name in Romanian
     */
    fun getMonthNameRo(): String {
        val months = listOf(
            "Ianuarie", "Februarie", "Martie", "Aprilie", "Mai", "Iunie",
            "Iulie", "August", "Septembrie", "Octombrie", "Noiembrie", "Decembrie"
        )
        return months[getMonth() - 1]
    }
}

@Serializable
enum class TransactionType {
    INCOME,
    EXPENSE
}
