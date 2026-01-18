package com.asr.financial.di

import com.asr.financial.platform.*
import org.koin.dsl.module

/**
 * iOS platform module
 * Provides iOS-specific implementations
 */
actual val platformModule = module {
    single { Clock() }
    single { SecureStorage() }
    single { FileHandler() }
    single { ImageCompressor() }
    single { Logger() }
    single { ImageCapture() }
    single { FileSharer() }
    single { FirebaseAuth() }
}
