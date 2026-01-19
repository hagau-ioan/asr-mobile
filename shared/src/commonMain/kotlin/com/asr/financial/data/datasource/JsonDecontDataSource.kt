package com.asr.financial.data.datasource

import com.asr.financial.domain.models.Decont
import com.asr.financial.domain.models.DecontResponse
import com.asr.financial.platform.ResourceLoader
import kotlinx.serialization.json.Json

/**
 * JSON file implementation of DecontDataSource.
 * Loads data from local JSON resource file for testing.
 */
class JsonDecontDataSource(
    private val resourceLoader: ResourceLoader
) : DecontDataSource {

    private companion object {
        const val FILE_NAME = "decont.json"
    }

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getAll(): List<Decont> {
        val jsonString = resourceLoader.loadResourceAsString(FILE_NAME)
            ?: throw IllegalStateException("data_error_load_failed")

        return try {
            // Try to decode as wrapped JSON first
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
