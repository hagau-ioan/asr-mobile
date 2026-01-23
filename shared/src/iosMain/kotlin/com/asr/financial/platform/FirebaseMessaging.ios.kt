@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.asr.financial.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.time.Clock

/**
 * iOS implementation of FirebaseMessaging using a bridge pattern.
 * 
 * This uses FirebaseMessagingBridge (Swift) which must be called from the iOS app layer.
 * The bridge is accessed via a singleton pattern similar to FirebaseAuthBridge.
 * 
 * Note: The Swift bridge (FirebaseMessagingBridge.swift) must be added to the Xcode project
 * and the Firebase Messaging iOS SDK must be added via Swift Package Manager.
 */
actual class FirebaseMessaging actual constructor(
    private val logger: Logger
) {
    
    private companion object {
        const val TAG = "FirebaseMessaging"
    }
    
    actual suspend fun getToken(): String? = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
            FirebaseMessagingBridge.getToken { token, error ->
                if (token != null) {
                    logger.debug(TAG, "FCM token retrieved successfully: $token")
                } else {
                    logger.error(TAG, "Failed to get FCM token: $error")
                }
                continuation.resume(token)
            }
        }
    }
    
    actual suspend fun deleteToken() = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
            FirebaseMessagingBridge.deleteToken { success ->
                if (success) {
                    logger.debug(TAG, "FCM token deleted successfully")
                } else {
                    logger.error(TAG, "Failed to delete FCM token")
                }
                continuation.resume(Unit)
            }
        }
    }
    
    actual suspend fun subscribeToTopic(topic: String): Boolean = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
            logger.debug(TAG, "Subscribing to topic: $topic")
            FirebaseMessagingBridge.subscribeToTopic(topic) { success ->
                if (success) {
                    logger.debug(TAG, "Successfully subscribed to topic: $topic")
                } else {
                    logger.error(TAG, "Failed to subscribe to topic: $topic")
                }
                continuation.resume(success)
            }
        }
    }
    
    actual suspend fun unsubscribeFromTopic(topic: String): Boolean = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
            logger.debug(TAG, "Unsubscribing from topic: $topic")
            FirebaseMessagingBridge.unsubscribeFromTopic(topic) { success ->
                if (success) {
                    logger.debug(TAG, "Successfully unsubscribed from topic: $topic")
                } else {
                    logger.error(TAG, "Failed to unsubscribe from topic: $topic")
                }
                continuation.resume(success)
            }
        }
    }
    
    actual fun setOnTokenRefreshCallback(onTokenRefresh: (String) -> Unit) {
        logger.debug(TAG, "Setting token refresh callback")
        FirebaseMessagingBridge.setOnTokenRefreshCallback { token ->
            logger.debug(TAG, "Token refreshed: $token")
            onTokenRefresh(token)
        }
    }
    
    actual fun setOnNotificationReceivedCallback(onNotificationReceived: (PushNotification) -> Unit) {
        logger.debug(TAG, "Setting notification received callback")
        FirebaseMessagingBridge.setOnNotificationReceivedCallback { title, body, data ->
            val currentTime = Clock.System.now().toEpochMilliseconds()
            val notification = PushNotification(
                id = currentTime.toString(),
                title = title ?: "",
                body = body ?: "",
                data = data ?: emptyMap(),
                timestamp = currentTime,
                topic = null
            )
            logger.debug(TAG, "Notification received: ${notification.title}")
            onNotificationReceived(notification)
        }
    }
}

/**
 * Bridge object for iOS Firebase Messaging communication.
 * This object provides static methods that will be called by the Swift bridge.
 * The Swift FirebaseMessagingBridge class should call these methods.
 */
object FirebaseMessagingBridge {
    
    // Get token callback
    private var getTokenCallback: ((String?, String?) -> Unit)? = null
    
    // Delete token callback
    private var deleteTokenCallback: ((Boolean) -> Unit)? = null
    
    // Subscribe to topic callback
    private var subscribeToTopicCallback: ((Boolean) -> Unit)? = null
    
    // Unsubscribe from topic callback
    private var unsubscribeFromTopicCallback: ((Boolean) -> Unit)? = null
    
    // Token refresh callback
    private var tokenRefreshCallback: ((String) -> Unit)? = null
    
    // Notification received callback
    private var notificationReceivedCallback: ((String?, String?, Map<String, String>?) -> Unit)? = null
    
    /**
     * Called from Kotlin to get FCM token.
     */
    fun getToken(completion: (String?, String?) -> Unit) {
        getTokenCallback = completion
        platform.Foundation.NSNotificationCenter.defaultCenter.postNotificationName(
            "FirebaseMessagingGetToken",
            `object` = null
        )
    }
    
    /**
     * Called from Kotlin to delete FCM token.
     */
    fun deleteToken(completion: (Boolean) -> Unit) {
        deleteTokenCallback = completion
        platform.Foundation.NSNotificationCenter.defaultCenter.postNotificationName(
            "FirebaseMessagingDeleteToken",
            `object` = null
        )
    }
    
    /**
     * Called from Kotlin to subscribe to a topic.
     */
    fun subscribeToTopic(topic: String, completion: (Boolean) -> Unit) {
        subscribeToTopicCallback = completion
        platform.Foundation.NSNotificationCenter.defaultCenter.postNotificationName(
            "FirebaseMessagingSubscribeToTopic",
            `object` = mapOf("topic" to topic)
        )
    }
    
    /**
     * Called from Kotlin to unsubscribe from a topic.
     */
    fun unsubscribeFromTopic(topic: String, completion: (Boolean) -> Unit) {
        unsubscribeFromTopicCallback = completion
        platform.Foundation.NSNotificationCenter.defaultCenter.postNotificationName(
            "FirebaseMessagingUnsubscribeFromTopic",
            `object` = mapOf("topic" to topic)
        )
    }
    
    /**
     * Called from Kotlin to set token refresh callback.
     */
    fun setOnTokenRefreshCallback(callback: (String) -> Unit) {
        tokenRefreshCallback = callback
    }
    
    /**
     * Called from Kotlin to set notification received callback.
     */
    fun setOnNotificationReceivedCallback(callback: (String?, String?, Map<String, String>?) -> Unit) {
        notificationReceivedCallback = callback
    }
    
    /**
     * Called from Swift bridge when token is retrieved.
     */
    fun reportGetTokenResult(token: String?, error: String?) {
        getTokenCallback?.invoke(token, error)
        getTokenCallback = null
    }
    
    /**
     * Called from Swift bridge when token deletion completes.
     */
    fun reportDeleteTokenResult(success: Boolean) {
        deleteTokenCallback?.invoke(success)
        deleteTokenCallback = null
    }
    
    /**
     * Called from Swift bridge when topic subscription completes.
     */
    fun reportSubscribeToTopicResult(success: Boolean) {
        subscribeToTopicCallback?.invoke(success)
        subscribeToTopicCallback = null
    }
    
    /**
     * Called from Swift bridge when topic unsubscription completes.
     */
    fun reportUnsubscribeFromTopicResult(success: Boolean) {
        unsubscribeFromTopicCallback?.invoke(success)
        unsubscribeFromTopicCallback = null
    }
    
    /**
     * Called from Swift bridge when token is refreshed.
     */
    fun notifyTokenRefresh(token: String) {
        tokenRefreshCallback?.invoke(token)
    }
    
    /**
     * Called from Swift bridge when notification is received.
     */
    fun notifyNotificationReceived(title: String?, body: String?, data: Map<String, String>?) {
        notificationReceivedCallback?.invoke(title, body, data)
    }
}
