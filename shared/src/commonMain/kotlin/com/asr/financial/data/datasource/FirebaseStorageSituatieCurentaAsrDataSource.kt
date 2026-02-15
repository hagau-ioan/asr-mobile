package com.asr.financial.data.datasource

import com.asr.financial.domain.models.SituatieCurentaAsr
import com.asr.financial.platform.FirebaseStorage
import kotlinx.serialization.json.Json

/**
 * Firebase Cloud Storage implementation of SituatieCurentaAsrDataSource.
 * Loads data from Firebase Storage: app/situatie_curenta_asr.json
 * using platform-specific FirebaseStorage.
 */
class FirebaseStorageSituatieCurentaAsrDataSource(
    private val firebaseStorage: FirebaseStorage
) : SituatieCurentaAsrDataSource {

    private companion object {
        const val FILE_PATH = "app/situatie_curenta_asr.json"
    }

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun get(): SituatieCurentaAsr? {
        val jsonString = firebaseStorage.downloadFileAsString(FILE_PATH) ?: return null
        return try {
            val wrapped = json.decodeFromString<JsonResponseWrapper<SituatieCurentaAsr>>(jsonString)
            wrapped.data
        } catch (e: Exception) {
            try {
                json.decodeFromString<SituatieCurentaAsr>(jsonString)
            } catch (e2: Exception) {
                null
            }
        }
    }
}
