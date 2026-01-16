package com.asr.financial.data.datasource

import com.asr.financial.domain.models.CongregationInfo
import com.asr.financial.platform.ResourceLoader
import kotlinx.serialization.json.Json

/**
 * JSON file implementation of CongregationDataSource.
 * Loads data from congregations.json using platform-specific ResourceLoader.
 */
class JsonCongregationDataSource(
    private val resourceLoader: ResourceLoader
) : CongregationDataSource {

    private companion object {
        const val FILE_NAME = "congregations.json"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
        encodeDefaults = true
    }

    override suspend fun getAll(): List<CongregationInfo> {
        val jsonString = resourceLoader.loadResourceAsString(FILE_NAME) ?: return emptyList()
        return try {
            json.decodeFromString<List<CongregationInfo>>(jsonString)
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
