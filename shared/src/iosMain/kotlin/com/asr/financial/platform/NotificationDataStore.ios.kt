@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.asr.financial.platform

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import kotlin.io.path.Path

/**
 * iOS implementation of NotificationDataStore.
 * Uses PreferenceDataStoreFactory with NSFileManager documents directory.
 * Singleton pattern with lazy initialization to prevent file lock issues.
 */
actual class NotificationDataStore {
    actual val dataStore: DataStore<Preferences> by lazy {
        val documentsDir = NSFileManager.defaultManager.URLsForDirectory(
            NSDocumentDirectory,
            NSUserDomainMask
        ).firstOrNull() as? NSURL
        
        val documentsPath = documentsDir?.path ?: error("Unable to get documents directory")
        val filePath = "$documentsPath/notification_preferences.preferences_pb"
        
        PreferenceDataStoreFactory.createWithPath(
            produceFile = { filePath.toPath() }
        )
    }
}
