package com.asr.financial.platform

import org.jetbrains.compose.resources.ExperimentalResourceApi
import asr_financial.composeapp.generated.resources.Res

/**
 * Compose Resources implementation of ResourceLoader.
 * Uses Compose Multiplatform resources API to load JSON files.
 */
class ComposeResourceLoader : ResourceLoader {
    @OptIn(ExperimentalResourceApi::class)
    override suspend fun loadResourceAsString(fileName: String): String? {
        return try {
            Res.readBytes("files/$fileName").decodeToString()
        } catch (e: Exception) {
            null
        }
    }
}
