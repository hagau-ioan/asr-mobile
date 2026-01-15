package com.asr.financial.di

import com.asr.financial.platform.*
import org.koin.dsl.module

/**
 * Platform module - expect/actual pattern
 * Provides platform-specific implementations
 */
expect val platformModule: org.koin.core.module.Module
