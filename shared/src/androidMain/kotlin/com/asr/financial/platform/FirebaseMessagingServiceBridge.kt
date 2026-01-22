package com.asr.financial.platform

/**
 * Bridge object to allow FirebaseMessagingService to communicate with FirebaseMessaging instance.
 * This is needed because FirebaseMessagingService runs in a separate process context.
 */
object FirebaseMessagingServiceBridge {
    
    @Volatile
    private var instance: FirebaseMessaging? = null
    
    @Volatile
    private var onTokenRefreshCallback: ((String) -> Unit)? = null
    
    @Volatile
    private var onNotificationReceivedCallback: ((PushNotification) -> Unit)? = null
    
    /**
     * Set the FirebaseMessaging instance (called when Koin initializes)
     */
    fun setInstance(messaging: FirebaseMessaging) {
        instance = messaging
    }
    
    /**
     * Get the FirebaseMessaging instance
     */
    fun getInstance(): FirebaseMessaging? = instance
    
    /**
     * Set token refresh callback
     */
    fun setOnTokenRefreshCallback(callback: (String) -> Unit) {
        onTokenRefreshCallback = callback
    }
    
    /**
     * Set notification received callback
     */
    fun setOnNotificationReceivedCallback(callback: (PushNotification) -> Unit) {
        onNotificationReceivedCallback = callback
    }
    
    /**
     * Notify about token refresh (called from FirebaseMessagingService)
     */
    fun notifyTokenRefresh(token: String) {
        onTokenRefreshCallback?.invoke(token)
    }
    
    /**
     * Notify about notification received (called from FirebaseMessagingService)
     */
    fun notifyNotificationReceived(notification: PushNotification) {
        onNotificationReceivedCallback?.invoke(notification)
    }
}
