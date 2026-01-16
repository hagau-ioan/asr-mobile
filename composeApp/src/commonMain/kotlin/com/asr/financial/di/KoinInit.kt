package com.asr.financial.di

import com.asr.financial.di.dataModule
import com.asr.financial.di.domainModule
import com.asr.financial.di.platformModule
import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(
            platformModule,
            dataModule,
            domainModule,
            presentationModule
        )
    }
}
