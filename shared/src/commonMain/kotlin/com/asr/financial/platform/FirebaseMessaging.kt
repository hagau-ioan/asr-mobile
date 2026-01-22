package com.asr.financial.platform

/**
 * Platform abstraction for Firebase Cloud Messaging (FCM).
 * Provides methods to manage FCM tokens and topic subscriptions.
 * 
 * Actual implementations:
 * - Android: Uses Firebase Messaging Android SDK
 * - iOS: Uses Firebase Messaging iOS SDK via Swift bridge
 */
expect class FirebaseMessaging(logger: Logger) {
    /**
     * Get the current FCM token.
     * @return FCM token string, or null if not available
     */
    suspend fun getToken(): String?
    
    /**
     * Delete the current FCM token.
     */
    suspend fun deleteToken()
    
    /**
     * Subscribe to a topic.
     * @param topic Topic name (e.g., "all_users", "admins")
     * @return true if subscription successful, false otherwise
     */
    suspend fun subscribeToTopic(topic: String): Boolean
    
    /**
     * Unsubscribe from a topic.
     * @param topic Topic name
     * @return true if unsubscription successful, false otherwise
     */
    suspend fun unsubscribeFromTopic(topic: String): Boolean
    
    /**
     * Set a callback for when a new token is generated.
     * This is called when the token is refreshed automatically.
     * @param onTokenRefresh Callback with new token
     */
    fun setOnTokenRefreshCallback(onTokenRefresh: (String) -> Unit)
    
    /**
     * Set a callback for when a notification is received in foreground.
     * Background notifications are handled by the platform service.
     * @param onNotificationReceived Callback with notification data
     */
    fun setOnNotificationReceivedCallback(onNotificationReceived: (PushNotification) -> Unit)
}

/**
 * Push notification data model
 */
data class PushNotification(
    val id: String,
    val title: String,
    val body: String,
    val data: Map<String, String> = emptyMap(),
    val timestamp: Long,
    val topic: String? = null
)
