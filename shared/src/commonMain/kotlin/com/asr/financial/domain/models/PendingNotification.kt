package com.asr.financial.domain.models

import kotlinx.serialization.Serializable

/**
 * Model for pending notification to be displayed in the app.
 * This represents a notification that was tapped and should be shown
 * in a banner on the Home screen until dismissed by the user.
 */
@Serializable
data class PendingNotification(
    val id: String,
    val title: String,
    val body: String,
    val url: String? = null,
    val timestamp: Long
)
