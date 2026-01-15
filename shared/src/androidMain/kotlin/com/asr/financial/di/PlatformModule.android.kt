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
}
