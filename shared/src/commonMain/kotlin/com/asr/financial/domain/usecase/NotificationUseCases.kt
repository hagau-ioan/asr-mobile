package com.asr.financial.domain.usecase

import com.asr.financial.domain.models.PendingNotification
import com.asr.financial.domain.repository.NotificationRepository
import com.asr.financial.platform.Clock
import com.asr.financial.platform.Logger
import com.asr.financial.platform.PushNotification
import kotlinx.coroutines.flow.Flow

/**
 * Use case for getting FCM token
 */
class GetFcmTokenUseCase(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(): String? = repository.getToken()
}

/**
 * Use case for subscribing to a topic
 */
class SubscribeToTopicUseCase(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(topic: String): Boolean {
        val success = repository.subscribeToTopic(topic)
        if (success) {
            // Update local list of subscribed topics
            val currentTopics = repository.getSubscribedTopics()
            if (!currentTopics.contains(topic)) {
                repository.saveSubscribedTopics(currentTopics + topic)
            }
        }
        return success
    }
}

/**
 * Use case for unsubscribing from a topic
 */
class UnsubscribeFromTopicUseCase(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(topic: String): Boolean {
        val success = repository.unsubscribeFromTopic(topic)
        if (success) {
            // Update local list of subscribed topics
            val currentTopics = repository.getSubscribedTopics()
            repository.saveSubscribedTopics(currentTopics.filter { it != topic })
        }
        return success
    }
}

/**
 * Use case for getting subscribed topics
 */
class GetSubscribedTopicsUseCase(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(): List<String> = repository.getSubscribedTopics()
}

/**
 * Use case for initializing FCM subscriptions based on user role
 * Automatically subscribes to "all_users" and "admins" if user is admin
 */
class InitializeFcmSubscriptionsUseCase(
    private val repository: NotificationRepository,
    private val isAdminUseCase: IsAdminUseCase
) {
    suspend operator fun invoke(): Boolean {
        return try {
            // Always subscribe to "all_users"
            repository.subscribeToTopic("all_users")
            
            // Subscribe to "admins" if user is admin
            val isAdmin = isAdminUseCase()
            if (isAdmin) {
                repository.subscribeToTopic("admins")
            }
            
            // Save subscribed topics
            val topics = mutableListOf("all_users")
            if (isAdmin) {
                topics.add("admins")
            }
            repository.saveSubscribedTopics(topics)
            
            true
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Use case for getting pending notification
 */
class GetPendingNotificationUseCase(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke() = repository.getPendingNotification()
}

/**
 * Use case for saving pending notification
 */
class SavePendingNotificationUseCase(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(notification: com.asr.financial.domain.models.PendingNotification) {
        repository.savePendingNotification(notification)
    }
}

/**
 * Use case for clearing pending notification
 */
class ClearPendingNotificationUseCase(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke() {
        repository.clearPendingNotification()
    }
}

/**
 * Use case for observing pending notification changes as a Flow.
 * Presentation layer should use this instead of accessing Repository directly.
 */
class ObservePendingNotificationUseCase(
    private val repository: NotificationRepository
) {
    operator fun invoke(): Flow<PendingNotification?> = repository.observePendingNotification()
}

/**
 * Use case for converting PushNotification to PendingNotification and saving it.
 * Handles the conversion logic and delegates to SavePendingNotificationUseCase.
 */
class ConvertAndSavePendingNotificationUseCase(
    private val saveUseCase: SavePendingNotificationUseCase,
    private val logger: Logger
) {
    suspend operator fun invoke(pushNotification: PushNotification) {
        val url = pushNotification.data["link"]
        val pendingNotification = PendingNotification(
            id = pushNotification.id,
            title = pushNotification.title,
            body = pushNotification.body,
            url = url,
            timestamp = pushNotification.timestamp
        )
        saveUseCase(pendingNotification)
    }
}

/**
 * Use case for converting Intent extras (Map<String, String>) to PendingNotification and saving it.
 * Used when notification is tapped and app opens from Intent extras.
 * This is platform-specific (Android), so we use a generic approach with Map<String, String>.
 */
class ConvertIntentDataAndSavePendingNotificationUseCase(
    private val saveUseCase: SavePendingNotificationUseCase,
    private val clock: Clock
) {
    /**
     * Convert notification data from Intent extras to PendingNotification and save it.
     * @param notificationData Map of string key-value pairs from Intent extras
     * @param getStringExtra Function to get string extra by key (platform-specific)
     * @param getLongExtra Function to get long extra by key with default (platform-specific)
     * @return true if notification was saved successfully, false otherwise
     */
    suspend operator fun invoke(
        notificationData: Map<String, String>,
        getStringExtra: (String) -> String? = { notificationData[it] },
        getLongExtra: (String, Long) -> Long = { key, default -> 
            notificationData[key]?.toLongOrNull() ?: default 
        }
    ): Boolean {
        return try {
            // Extract notification data from map or extras
            val notificationId = getStringExtra("notification_id")
                ?: clock.now().toEpochMilliseconds().toString()

            val notificationTitle = getStringExtra("notification_title")
                ?: notificationData["title"]
                ?: ""
            
            val notificationBody = getStringExtra("notification_body")
                ?: notificationData["body"]
                ?: ""
            val notificationUrl = notificationData["link"]
            
            val notificationTimestamp = getLongExtra("notification_timestamp", -1L)
                .takeIf { it > 0 }
                ?: clock.now().toEpochMilliseconds()
            
            // Only save if we have valid title and body
            if (notificationTitle.isNotBlank() && notificationBody.isNotBlank()) {
                val notification = PendingNotification(
                    id = notificationId,
                    title = notificationTitle,
                    body = notificationBody,
                    url = notificationUrl,
                    timestamp = notificationTimestamp
                )
                saveUseCase(notification)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
