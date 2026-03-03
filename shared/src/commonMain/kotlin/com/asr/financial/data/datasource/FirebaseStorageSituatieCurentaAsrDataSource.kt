package com.asr.financial.data.datasource

import com.asr.financial.domain.models.SituatieCurentaAsr
import com.asr.financial.platform.FirebaseStorage
import com.asr.financial.platform.Logger
import kotlinx.serialization.json.Json

/**
 * Firebase Cloud Storage implementation of SituatieCurentaAsrDataSource.
 * Loads data from Firebase Storage: app/situatie_curenta_asr.json
 * using platform-specific FirebaseStorage.
 */
class FirebaseStorageSituatieCurentaAsrDataSource(
    private val firebaseStorage: FirebaseStorage,
    private val logger: Logger
) : SituatieCurentaAsrDataSource {

    private companion object {
        const val FILE_PATH = "app/situatie_curenta_asr.json"
        const val TAG = "SituatieCurentaAsrDS"
    }

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun get(): SituatieCurentaAsr? {
        logger.debug(TAG, "Loading situatie_curenta_asr from Cloud: $FILE_PATH")
        val jsonString = firebaseStorage.downloadFileAsString(FILE_PATH)
        if (jsonString == null) {
            logger.error(
                TAG,
                "Failed to download situatie_curenta_asr from Cloud (download returned null). " +
                    "Check: 1) Firebase Storage rules allow read for authenticated users on app/ 2) File exists at $FILE_PATH 3) User is logged in and token valid.",
                null
            )
            return null
        }
        logger.debug(TAG, "Downloaded ${jsonString.length} bytes from Cloud, parsing JSON")
        return try {
            val wrapped = json.decodeFromString<JsonResponseWrapper<SituatieCurentaAsr>>(jsonString)
            logger.debug(TAG, "Parsed situatie_curenta_asr (wrapped format) successfully")
            wrapped.data
        } catch (e: Exception) {
            logger.warning(TAG, "Wrapped format parse failed: ${e.message}, trying direct format")
            try {
                val direct = json.decodeFromString<SituatieCurentaAsr>(jsonString)
                logger.debug(TAG, "Parsed situatie_curenta_asr (direct format) successfully")
                direct
            } catch (e2: Exception) {
                logger.error(
                    TAG,
                    "Failed to parse situatie_curenta_asr JSON: ${e2.message}. First 200 chars: ${jsonString.take(200)}",
                    e2
                )
                null
            }
        }
    }
}
