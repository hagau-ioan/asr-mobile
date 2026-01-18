package com.asr.financial.data.datasource

import kotlinx.serialization.Serializable

/**
 * Generic wrapper for JSON responses from Firebase Storage.
 * All JSON files now follow this structure with _meta and data fields.
 */
@Serializable
data class JsonResponseWrapper<T>(
    val _meta: MetaInfo,
    val data: T
)

@Serializable
data class MetaInfo(
    val version: String,
    val generated_at: String
)
