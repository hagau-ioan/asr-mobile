package com.asr.financial.domain.models

import kotlinx.datetime.Instant

/**
 * Domain model for Utility Expense
 */
@Suppress("DEPRECATION") // kotlinx.datetime.Instant is the correct multiplatform type
data class UtilityExpense(
    val id: Long = 0,
    val utilityType: UtilityType,
    val amount: Double,
    val month: Int,
    val year: Int,
    val meterReading: String? = null,
    val receiptImagePath: String? = null,
    val notes: String? = null,
    val createdAt: Instant
)

enum class UtilityType {
    ELECTRICITY,
    WATER,
    GAS,
    INTERNET,
    PHONE,
    OTHER
}
