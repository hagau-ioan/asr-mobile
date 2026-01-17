package com.asr.financial.di

import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    if (GlobalContext.getOrNull() == null) {
        startKoin {
            appDeclaration()
            modules(
                platformModule,
                dataModule,
                domainModule,
                presentationModule
            )
        }
    }
}
