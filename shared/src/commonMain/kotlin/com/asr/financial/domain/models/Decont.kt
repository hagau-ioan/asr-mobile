package com.asr.financial.domain.models

import kotlinx.serialization.Serializable

/**
 * Decont (expense report) model containing expenses for a specific month/year
 */
@Serializable
data class Decont(
    val year: Int,
    val month: Int,
    val expenses: List<DecontExpense>
)

/**
 * Individual expense within a decont
 */
@Serializable
data class DecontExpense(
    val receipt_nr: String,
    val date: String,
    val company: String,
    val amount: Double,
    val product_names: List<DecontProductName> = emptyList()
)

/**
 * Product name within a decont expense
 */
@Serializable
data class DecontProductName(
    val name: String
)

/**
 * Response wrapper for decont data from backend
 */
@Serializable
data class DecontResponse(
    val deconts: List<Decont>
)
