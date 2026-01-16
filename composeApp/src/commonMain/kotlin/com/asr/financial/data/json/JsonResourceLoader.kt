package com.asr.financial.data.json

import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import asr_financial.composeapp.generated.resources.Res

/**
 * Centralized utility for loading JSON files from Compose resources.
 * All JSON DataSource implementations use this loader.
 */
object JsonResourceLoader {

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
        encodeDefaults = true
    }

    /**
     * Load JSON file content as string from resources
     * @param fileName Name of the JSON file (e.g., "transactions.json")
     * @return JSON string content, or null if loading fails
     */
    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadJsonString(fileName: String): String? {
        return try {
            Res.readBytes("files/$fileName").decodeToString()
        } catch (e: Exception) {
            null
        }
    }
}
