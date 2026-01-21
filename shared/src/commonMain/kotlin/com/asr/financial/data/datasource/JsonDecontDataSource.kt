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

        // Parse as wrapped JSON with direct List<Decont> (API format: { "_meta": {...}, "data": [...] })
        val wrapped = json.decodeFromString<JsonResponseWrapper<List<Decont>>>(jsonString)
        return wrapped.data
    }

    override suspend fun getByMonth(year: Int, month: Int): Decont? {
        return getAll().find { it.year == year && it.month == month }
    }
}
