package com.asr.financial.domain.repository

import com.asr.financial.domain.models.PendingNotification
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for FCM notifications.
 * Handles FCM token management, topic subscriptions, and pending notifications.
 */
interface NotificationRepository {
    /**
     * Get the current FCM token.
     * @return FCM token string, or null if not available
     */
    suspend fun getToken(): String?
    
    /**
     * Save FCM token (for persistence if needed).
     * @param token FCM token to save
     * @return true if saved successfully, false otherwise
     */
    suspend fun saveToken(token: String): Boolean
    
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
     * Get list of subscribed topics (if stored locally).
     * @return List of topic names
     */
    suspend fun getSubscribedTopics(): List<String>
    
    /**
     * Save subscribed topics (for persistence).
     * @param topics List of topic names
     */
    suspend fun saveSubscribedTopics(topics: List<String>)
    
    /**
     * Get the current pending notification, if any.
     * @return PendingNotification if exists, null otherwise
     */
    suspend fun getPendingNotification(): PendingNotification?
    
    /**
     * Observe pending notification changes as a Flow.
     * Emits the current notification whenever it changes (save/clear).
     * @return Flow of PendingNotification? (null when cleared)
     */
    fun observePendingNotification(): Flow<PendingNotification?>
    
    /**
     * Save a pending notification to be displayed in the app.
     * @param notification The notification to save
     */
    suspend fun savePendingNotification(notification: PendingNotification)
    
    /**
     * Clear the pending notification (when user dismisses it).
     */
    suspend fun clearPendingNotification()
}
