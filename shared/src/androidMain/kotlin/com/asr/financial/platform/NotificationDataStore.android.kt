package com.asr.financial.platform

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import okio.Path.Companion.toPath
import java.io.File

/**
 * Android implementation of NotificationDataStore.
 * Uses PreferenceDataStoreFactory with context.filesDir.
 * Singleton pattern with lazy initialization to prevent file lock issues.
 */
actual class NotificationDataStore(
    private val context: Context
) {
    actual val dataStore: DataStore<Preferences> by lazy {
        val filePath = File(context.filesDir, "notification_preferences.preferences_pb").absolutePath
        PreferenceDataStoreFactory.createWithPath(
            produceFile = { filePath.toPath() }
        )
    }
}
