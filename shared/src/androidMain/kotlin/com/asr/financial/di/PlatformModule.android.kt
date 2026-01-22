package com.asr.financial.di

import com.asr.financial.platform.*
import org.koin.dsl.module

/**
 * Android platform module
 * Provides Android-specific implementations
 */
actual val platformModule = module {
    single { Clock() }
    single { SecureStorage(get()) }
    single { FileHandler(get()) }
    single { ImageCompressor() }
    single { Logger() }
    single { FileSharer(get()) }
    single { ImageCapture(get()) }
    single { Clipboard(get()) }
    single { FirebaseAuth() }
    single { FirebaseStorage(get()) }  // Logger injected for error logging
    single { FirebaseMessaging(get()) }  // Logger injected for error logging
    single { UrlLauncher(get()) }  // Context injected for opening URLs
    single { NotificationDataStore(get()) }  // Context injected for DataStore file path
}
