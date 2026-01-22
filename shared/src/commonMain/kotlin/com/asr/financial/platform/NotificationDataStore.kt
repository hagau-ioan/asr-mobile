package com.asr.financial.platform

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

/**
 * Platform abstraction for Notification DataStore.
 * Android: Uses PreferenceDataStoreFactory with context.filesDir
 * iOS: Uses PreferenceDataStoreFactory with NSFileManager documents directory
 */
expect class NotificationDataStore {
    /**
     * Get the DataStore instance for storing notification preferences.
     */
    val dataStore: DataStore<Preferences>
}
