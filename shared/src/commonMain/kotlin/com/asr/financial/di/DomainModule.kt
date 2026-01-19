package com.asr.financial.di

import com.asr.financial.domain.usecase.*
import org.koin.dsl.module

/**
 * Domain module
 * Provides use cases
 */
val domainModule = module {
    // Transaction use cases
    factory { GetTransactionsUseCase(get()) }
    factory { GetTransactionsByMonthUseCase(get()) }

    // Congregation use cases
    factory { GetAllCongregationsUseCase(get()) }
    factory { GetCongregationNamesUseCase(get()) }
    factory { GetCongregationCountUseCase(get()) }
    factory { GetTotalMemberCountUseCase(get()) }

    // AppConfig use cases
    factory { GetAppConfigUseCase(get()) }
    factory { GetAvailableYearsUseCase(get()) }
    factory { GetExpectedDonationUseCase(get()) }
    factory { GetTotalPublishersUseCase(get()) }
    factory { GetPublisherExpectedContributionUseCase(get()) }
    factory { GetStartYearUseCase(get()) }
    factory {
        RefreshDataUseCase(
            transactionRepository = get(),
            appConfigRepository = get(),
            congregationRepository = get(),
            asrExpenseRepository = get(),
            decontRepository = get()
        )
    }
    
    // ASR Expenses use cases
    factory { GetAsrExpensesUseCase(get()) }

    // Decont use cases
    factory { GetDecontUseCase(get()) }
    
    // Auth use cases
    factory { LoginUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { CheckAuthStatusUseCase(get()) }
    factory { GetCurrentUserUseCase(get()) }
    factory { RefreshTokenUseCase(get()) }
    factory { GetUserRoleUseCase(get()) }
    factory { IsAdminUseCase(get()) }
    factory { CheckPermissionUseCase(get()) }
}
