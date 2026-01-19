package com.asr.financial.data.datasource

import com.asr.financial.domain.models.CongregationInfo
import com.asr.financial.platform.FirebaseStorage
import kotlinx.serialization.json.Json

/**
 * Firebase Cloud Storage implementation of CongregationDataSource.
 * Loads data from Firebase Storage: json/congregations.json
 * using platform-specific FirebaseStorage.
 */
class FirebaseStorageCongregationDataSource(
    private val firebaseStorage: FirebaseStorage
) : CongregationDataSource {

    private companion object {
        const val FILE_PATH = "app/congregations.json"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
        encodeDefaults = true
    }

    override suspend fun getAll(): List<CongregationInfo> {
        val jsonString = firebaseStorage.downloadFileAsString(FILE_PATH) ?: return emptyList()
        return try {
            // Try to decode as wrapped JSON first (Firebase Storage format)
            val wrapped = json.decodeFromString<JsonResponseWrapper<List<CongregationInfo>>>(jsonString)
            wrapped.data
        } catch (_: Exception) {
            // Fallback to direct array (for backward compatibility with local files)
            try {
                json.decodeFromString<List<CongregationInfo>>(jsonString)
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    override suspend fun getActive(): List<CongregationInfo> {
        return getAll().filter { it.isActive }
    }

    override suspend fun getById(id: String): CongregationInfo? {
        return getAll().firstOrNull { it.id == id }
    }

    override suspend fun getAllNames(): List<String> {
        return getAll().map { it.name }
    }

    override suspend fun getCount(): Int {
        return getAll().size
    }

    override suspend fun getTotalMemberCount(): Int {
        return getAll().sumOf { it.memberCount }
    }
}
