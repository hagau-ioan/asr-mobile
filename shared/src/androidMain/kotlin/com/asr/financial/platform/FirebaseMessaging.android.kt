package com.asr.financial.platform

import com.google.firebase.messaging.FirebaseMessaging as AndroidFirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Android implementation of FirebaseMessaging.
 * Uses Firebase Messaging Android SDK to manage FCM tokens and topics.
 */
actual class FirebaseMessaging actual constructor(
    private val logger: Logger
) {
    
    private val firebaseMessaging: AndroidFirebaseMessaging = AndroidFirebaseMessaging.getInstance()
    
    private var onTokenRefreshCallback: ((String) -> Unit)? = null
    private var onNotificationReceivedCallback: ((PushNotification) -> Unit)? = null
    
    private companion object {
        const val TAG = "FirebaseMessaging"
    }
    
    init {
        // Register this instance with the bridge
        FirebaseMessagingServiceBridge.setInstance(this)
    }
    
    actual suspend fun getToken(): String? = withContext(Dispatchers.IO) {
        try {
            logger.debug(TAG, "Getting FCM token")
            val token = firebaseMessaging.token.await()
            logger.debug(TAG, "FCM token retrieved successfully")
            token
        } catch (e: Exception) {
            logger.error(TAG, "Failed to get FCM token", e)
            null
        }
    }
    
    actual suspend fun deleteToken() = withContext(Dispatchers.IO) {
        try {
            logger.debug(TAG, "Deleting FCM token")
            firebaseMessaging.deleteToken().await()
            logger.debug(TAG, "FCM token deleted successfully")
        } catch (e: Exception) {
            logger.error(TAG, "Failed to delete FCM token", e)
        }
    }
    
    actual suspend fun subscribeToTopic(topic: String): Boolean = withContext(Dispatchers.IO) {
        try {
            logger.debug(TAG, "Subscribing to topic: $topic")
            firebaseMessaging.subscribeToTopic(topic).await()
            logger.debug(TAG, "Successfully subscribed to topic: $topic")
            true
        } catch (e: Exception) {
            logger.error(TAG, "Failed to subscribe to topic: $topic", e)
            false
        }
    }
    
    actual suspend fun unsubscribeFromTopic(topic: String): Boolean = withContext(Dispatchers.IO) {
        try {
            logger.debug(TAG, "Unsubscribing from topic: $topic")
            firebaseMessaging.unsubscribeFromTopic(topic).await()
            logger.debug(TAG, "Successfully unsubscribed from topic: $topic")
            true
        } catch (e: Exception) {
            logger.error(TAG, "Failed to unsubscribe from topic: $topic", e)
            false
        }
    }
    
    actual fun setOnTokenRefreshCallback(onTokenRefresh: (String) -> Unit) {
        logger.debug(TAG, "Setting token refresh callback")
        onTokenRefreshCallback = onTokenRefresh
        // Also register with bridge for service access
        FirebaseMessagingServiceBridge.setOnTokenRefreshCallback(onTokenRefresh)
    }
    
    actual fun setOnNotificationReceivedCallback(onNotificationReceived: (PushNotification) -> Unit) {
        logger.debug(TAG, "Setting notification received callback")
        onNotificationReceivedCallback = onNotificationReceived
        // Also register with bridge for service access
        FirebaseMessagingServiceBridge.setOnNotificationReceivedCallback(onNotificationReceived)
    }
    
    /**
     * Internal method to notify about token refresh (called from FirebaseMessagingService)
     */
    fun notifyTokenRefresh(token: String) {
        logger.debug(TAG, "Token refreshed: $token")
        onTokenRefreshCallback?.invoke(token)
    }
    
    /**
     * Internal method to notify about notification received (called from FirebaseMessagingService)
     */
    fun notifyNotificationReceived(notification: PushNotification) {
        logger.debug(TAG, "Notification received: ${notification.title}")
        onNotificationReceivedCallback?.invoke(notification)
    }
}
