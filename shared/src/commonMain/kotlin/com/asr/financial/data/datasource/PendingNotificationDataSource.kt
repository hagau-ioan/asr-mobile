package com.asr.financial.data.datasource

import com.asr.financial.domain.models.PendingNotification
import kotlinx.coroutines.flow.Flow

/**
 * Data source interface for Pending Notification data.
 * Handles storage and retrieval of pending notifications to be displayed in the app.
 */
interface PendingNotificationDataSource {
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
     * Save a pending notification.
     * @param notification The notification to save
     */
    suspend fun savePendingNotification(notification: PendingNotification)
    
    /**
     * Clear the pending notification.
     */
    suspend fun clearPendingNotification()
}
