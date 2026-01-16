package com.asr.financial.domain.usecase

import com.asr.financial.domain.model.Transaction
import com.asr.financial.domain.repository.TransactionRepository

/**
 * UseCase for getting all transactions
 */
class GetTransactionsUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(): List<Transaction> {
        return repository.getAllTransactions()
    }
}

/**
 * UseCase for getting transactions by month
 */
class GetTransactionsByMonthUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(month: Int, year: Int): List<Transaction> {
        return repository.getTransactionsByMonth(month, year)
    }
}
