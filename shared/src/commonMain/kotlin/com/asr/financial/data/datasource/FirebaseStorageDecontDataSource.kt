package com.asr.financial.data.datasource

import com.asr.financial.domain.models.Decont
import com.asr.financial.domain.models.DecontResponse
import com.asr.financial.platform.FirebaseStorage
import kotlinx.serialization.json.Json

/**
 * Firebase Cloud Storage implementation of DecontDataSource.
 * Loads data from Firebase Storage: app/decont.json
 * using platform-specific FirebaseStorage.
 */
class FirebaseStorageDecontDataSource(
    private val firebaseStorage: FirebaseStorage
) : DecontDataSource {

    private companion object {
        const val FILE_PATH = "app/decont.json"
    }

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getAll(): List<Decont> {
        // Load directly from Firebase Storage (caching is handled at repository level)
        val jsonString = firebaseStorage.downloadFileAsString(FILE_PATH)
            ?: throw IllegalStateException("data_error_load_failed")
        return try {
            // Try to decode as wrapped JSON first (Firebase Storage format)
            val wrapped = json.decodeFromString<JsonResponseWrapper<DecontResponse>>(jsonString)
            wrapped.data.deconts
        } catch (e: Exception) {
            // Fallback to direct DecontResponse (for backward compatibility)
            try {
                val response = json.decodeFromString<DecontResponse>(jsonString)
                response.deconts
            } catch (e2: Exception) {
                emptyList()
            }
        }
    }

    override suspend fun getByMonth(year: Int, month: Int): Decont? {
        return getAll().find { it.year == year && it.month == month }
    }
}
