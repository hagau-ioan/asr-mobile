package com.asr.financial.android

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.asr.financial.platform.Clock
import com.asr.financial.platform.FirebaseMessagingServiceBridge
import com.asr.financial.platform.PushNotification
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Firebase Messaging Service for handling FCM notifications.
 * Handles both foreground and background notifications.
 */
class AsrFirebaseMessagingService : FirebaseMessagingService() {
    
    private companion object {
        const val CHANNEL_ID = "asr_financial_default"
        const val CHANNEL_NAME = "ASR Financial Notifications"
        const val NOTIFICATION_ID = 1
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Notify FirebaseMessaging about token refresh via bridge
        // The bridge will notify FirebaseMessaging instance which handles token persistence via Repository
        FirebaseMessagingServiceBridge.notifyTokenRefresh(token)
    }
    
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        val notification = message.notification
        val data = message.data
        val clock = Clock() // Create Clock instance for platform abstraction
        
        if (notification != null) {
            // Convert RemoteMessage to PushNotification
            val pushNotification = PushNotification(
                id = message.messageId ?: clock.now().toEpochMilliseconds().toString(),
                title = notification.title ?: "",
                body = notification.body ?: "",
                data = data,
                timestamp = clock.now().toEpochMilliseconds(),
                topic = message.from?.substringAfterLast("/")
            )
            
            // Notify FirebaseMessaging about notification received via bridge
            // The bridge will notify FirebaseMessaging instance which handles saving via UseCase in App.kt
            FirebaseMessagingServiceBridge.notifyNotificationReceived(pushNotification)
            
            // Show notification in system tray
            showNotification(pushNotification)
        }
    }
    
    private fun createNotificationChannel() {
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications from ASR Financial app"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 300, 200, 300)
            setSound(soundUri, audioAttributes)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
    
    private fun showNotification(notification: PushNotification) {
        // Always open MainActivity when notification is tapped
        // The notification will be stored and displayed in a banner on Home screen
        // Use FLAG_ACTIVITY_NEW_TASK and FLAG_ACTIVITY_SINGLE_TOP to avoid restarting app if already open
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            // Add all notification data for storage and display
            putExtra("notification_id", notification.id)
            putExtra("notification_title", notification.title)
            putExtra("notification_body", notification.body)
            putExtra("notification_timestamp", notification.timestamp)
            notification.data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notification.body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 300, 200, 300))
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }
}
