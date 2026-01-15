package com.asr.financial.domain.models

/**
 * Domain model for Financial Period
 */
data class FinancialPeriod(
    val month: Int,
    val year: Int
) {
    init {
        require(month in 1..12) { "Month must be between 1 and 12" }
        require(year > 0) { "Year must be positive" }
    }
    
    override fun toString(): String = "$month/$year"
}
