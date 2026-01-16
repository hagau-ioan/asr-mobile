package com.asr.financial.data

import com.asr.financial.data.repository.JsonLoader
import org.jetbrains.compose.resources.ExperimentalResourceApi
import asr_financial.composeapp.generated.resources.Res

/**
 * Compose Multiplatform implementation of JsonLoader
 */
class ComposeJsonLoader : JsonLoader {
    @OptIn(ExperimentalResourceApi::class)
    override suspend fun loadJsonFromResources(fileName: String): String {
        return Res.readBytes("files/$fileName").decodeToString()
    }
}
