package com.asr.financial.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.asr.financial.domain.models.PendingNotification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * DataStore implementation of PendingNotificationDataSource.
 * Uses DataStore Preferences for storing pending notification data.
 */
class DataStorePendingNotificationDataSource(
    private val dataStore: DataStore<Preferences>
) : PendingNotificationDataSource {
    
    private companion object {
        val KEY_NOTIFICATION_ID = stringPreferencesKey("pending_notification_id")
        val KEY_NOTIFICATION_TITLE = stringPreferencesKey("pending_notification_title")
        val KEY_NOTIFICATION_BODY = stringPreferencesKey("pending_notification_body")
        val KEY_NOTIFICATION_URL = stringPreferencesKey("pending_notification_url")
        val KEY_NOTIFICATION_TIMESTAMP = longPreferencesKey("pending_notification_timestamp")
    }
    
    /**
     * Helper function to convert Preferences to PendingNotification.
     * Eliminates code duplication between getPendingNotification and observePendingNotification.
     */
    private fun preferencesToPendingNotification(preferences: Preferences): PendingNotification? {
        return try {
            val id = preferences[KEY_NOTIFICATION_ID] ?: return null
            val title = preferences[KEY_NOTIFICATION_TITLE] ?: return null
            val body = preferences[KEY_NOTIFICATION_BODY] ?: return null
            val url = preferences[KEY_NOTIFICATION_URL]
            val timestamp = preferences[KEY_NOTIFICATION_TIMESTAMP] ?: return null
            
            PendingNotification(
                id = id,
                title = title,
                body = body,
                url = url,
                timestamp = timestamp
            )
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun getPendingNotification(): PendingNotification? {
        return try {
            val preferences = dataStore.data.first()
            preferencesToPendingNotification(preferences)
        } catch (e: Exception) {
            null
        }
    }
    
    override fun observePendingNotification(): Flow<PendingNotification?> {
        return dataStore.data.map { preferences ->
            preferencesToPendingNotification(preferences)
        }
    }
    
    override suspend fun savePendingNotification(notification: PendingNotification) {
        dataStore.edit { preferences ->
            // Only save if this notification is newer than the existing one (if any)
            // This ensures we always display the most recent notification
            val existingTimestamp = preferences[KEY_NOTIFICATION_TIMESTAMP]
            if (existingTimestamp == null || notification.timestamp >= existingTimestamp) {
                preferences[KEY_NOTIFICATION_ID] = notification.id
                preferences[KEY_NOTIFICATION_TITLE] = notification.title
                preferences[KEY_NOTIFICATION_BODY] = notification.body
                if (notification.url != null) {
                    preferences[KEY_NOTIFICATION_URL] = notification.url
                } else {
                    preferences.remove(KEY_NOTIFICATION_URL)
                }
                preferences[KEY_NOTIFICATION_TIMESTAMP] = notification.timestamp
            }
            // If existing notification is newer, keep it and don't save the new one
        }
    }
    
    override suspend fun clearPendingNotification() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_NOTIFICATION_ID)
            preferences.remove(KEY_NOTIFICATION_TITLE)
            preferences.remove(KEY_NOTIFICATION_BODY)
            preferences.remove(KEY_NOTIFICATION_URL)
            preferences.remove(KEY_NOTIFICATION_TIMESTAMP)
        }
    }
}
