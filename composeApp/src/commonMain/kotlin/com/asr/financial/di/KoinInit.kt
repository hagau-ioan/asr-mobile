package com.asr.financial.di

import com.asr.financial.di.dataModule
import com.asr.financial.di.domainModule
import com.asr.financial.di.platformModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
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
