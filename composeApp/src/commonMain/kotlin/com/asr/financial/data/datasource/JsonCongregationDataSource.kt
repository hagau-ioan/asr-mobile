package com.asr.financial.data.datasource

import com.asr.financial.data.json.JsonResourceLoader
import com.asr.financial.domain.models.CongregationInfo

/**
 * JSON file implementation of CongregationDataSource.
 * Loads data from congregations.json in Compose resources.
 */
class JsonCongregationDataSource : CongregationDataSource {

    private companion object {
        const val FILE_NAME = "congregations.json"
    }

    override suspend fun getAll(): List<CongregationInfo> {
        val jsonString = JsonResourceLoader.loadJsonString(FILE_NAME) ?: return emptyList()
        return try {
            JsonResourceLoader.json.decodeFromString<List<CongregationInfo>>(jsonString)
        } catch (e: Exception) {
            emptyList()
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
