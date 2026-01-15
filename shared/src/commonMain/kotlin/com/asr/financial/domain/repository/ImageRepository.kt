package com.asr.financial.domain.repository

import com.asr.financial.domain.models.ReceiptImage
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for ReceiptImage operations
 * Implementation in data layer
 */
interface ImageRepository {
    fun getAllImages(): Flow<List<ReceiptImage>>
    fun getImagesByExpense(expenseId: Long): Flow<List<ReceiptImage>>
    fun getImagesByUtilityExpense(utilityExpenseId: Long): Flow<List<ReceiptImage>>
    suspend fun getImageById(id: Long): ReceiptImage?
    suspend fun insertImage(image: ReceiptImage): Long
    suspend fun deleteImage(id: Long)
    suspend fun saveImageFile(fileName: String, imageData: ByteArray): String
    suspend fun deleteImageFile(filePath: String): Boolean
}
