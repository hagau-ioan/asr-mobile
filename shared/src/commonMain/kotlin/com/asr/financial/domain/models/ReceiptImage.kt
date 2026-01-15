package com.asr.financial.domain.models

import kotlinx.datetime.Instant

/**
 * Domain model for Receipt Image
 */
data class ReceiptImage(
    val id: Long = 0,
    val expenseId: Long? = null,
    val utilityExpenseId: Long? = null,
    val filePath: String,
    val fileName: String,
    val fileSize: Long,
    val createdAt: Instant
)
