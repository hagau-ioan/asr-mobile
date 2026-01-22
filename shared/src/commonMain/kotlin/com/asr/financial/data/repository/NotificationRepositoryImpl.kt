package com.asr.financial.data.repository

import com.asr.financial.data.datasource.PendingNotificationDataSource
import com.asr.financial.domain.models.PendingNotification
import com.asr.financial.domain.repository.NotificationRepository
import com.asr.financial.platform.FirebaseMessaging
import com.asr.financial.platform.Logger
import com.asr.financial.platform.SecureStorage
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Implementation of NotificationRepository.
 * Uses FirebaseMessaging for FCM operations, SecureStorage for token/topics persistence,
 * and PendingNotificationDataSource for pending notification storage.
 */
class NotificationRepositoryImpl(
    private val firebaseMessaging: FirebaseMessaging,
    private val secureStorage: SecureStorage,
    private val logger: Logger,
    private val pendingNotificationDataSource: PendingNotificationDataSource
) : NotificationRepository {

    private companion object {
        const val TAG = "NotificationRepository"
        const val TOKEN_STORAGE_KEY = "fcm_token"
        const val TOPICS_STORAGE_KEY = "fcm_subscribed_topics"
    }
    
    private val json = Json { ignoreUnknownKeys = true }
    
    @Serializable
    private data class TopicsData(val topics: List<String>)
    
    override suspend fun getToken(): String? {
        return firebaseMessaging.getToken()
    }
    
    override suspend fun saveToken(token: String): Boolean {
        return try {
            secureStorage.save(TOKEN_STORAGE_KEY, token)
            true
        } catch (e: Exception) {
            logger.error(TAG, "Failed to save FCM token", e)
            false
        }
    }
    
    override suspend fun subscribeToTopic(topic: String): Boolean {
        return firebaseMessaging.subscribeToTopic(topic)
    }
    
    override suspend fun unsubscribeFromTopic(topic: String): Boolean {
        return firebaseMessaging.unsubscribeFromTopic(topic)
    }
    
    override suspend fun getSubscribedTopics(): List<String> {
        return try {
            val topicsJson = secureStorage.get(TOPICS_STORAGE_KEY)
            if (topicsJson != null) {
                val topicsData = json.decodeFromString<TopicsData>(topicsJson)
                topicsData.topics
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            logger.error(TAG, "Failed to get subscribed topics", e)
            emptyList()
        }
    }
    
    override suspend fun saveSubscribedTopics(topics: List<String>) {
        try {
            val topicsData = TopicsData(topics)
            val topicsJson = json.encodeToString(TopicsData.serializer(), topicsData)
            secureStorage.save(TOPICS_STORAGE_KEY, topicsJson)
        } catch (e: Exception) {
            logger.error(TAG, "Failed to save subscribed topics", e)
        }
    }
    
    override suspend fun getPendingNotification(): PendingNotification? {
        return try {
            pendingNotificationDataSource.getPendingNotification()
        } catch (e: Exception) {
            logger.error(TAG, "Failed to get pending notification", e)
            null
        }
    }
    
    override fun observePendingNotification() = pendingNotificationDataSource.observePendingNotification()
    
    override suspend fun savePendingNotification(notification: PendingNotification) {
        try {
            pendingNotificationDataSource.savePendingNotification(notification)
        } catch (e: Exception) {
            logger.error(TAG, "Failed to save pending notification", e)
        }
    }
    
    override suspend fun clearPendingNotification() {
        try {
            pendingNotificationDataSource.clearPendingNotification()
        } catch (e: Exception) {
            logger.error(TAG, "Failed to clear pending notification", e)
        }
    }
}
