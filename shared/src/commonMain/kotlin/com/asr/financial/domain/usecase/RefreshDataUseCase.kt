package com.asr.financial.domain.usecase

import com.asr.financial.domain.repository.AppConfigRepository
import com.asr.financial.domain.repository.AsrExpenseRepository
import com.asr.financial.domain.repository.CongregationInfoRepository
import com.asr.financial.domain.repository.DecontRepository
import com.asr.financial.domain.repository.CurrentAsrSituationRepository
import com.asr.financial.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

/**
 * Use case to refresh all data from remote source.
 * Clears cache and reloads data from Firebase Storage for all repositories.
 * 
 * Uses supervisorScope to ensure that if one repository refresh fails,
 * the others will still complete successfully.
 */
class RefreshDataUseCase(
    private val transactionRepository: TransactionRepository,
    private val appConfigRepository: AppConfigRepository,
    private val congregationRepository: CongregationInfoRepository,
    private val asrExpenseRepository: AsrExpenseRepository,
    private val decontRepository: DecontRepository,
    private val currentAsrSituationRepository: CurrentAsrSituationRepository
) {
    suspend operator fun invoke() {
        // Exception handler to log errors without cancelling other operations
        val exceptionHandler = CoroutineExceptionHandler { _, exception ->
            // Log the error - in production, you might want to use a proper logger
            // For now, we silently handle errors so other refreshes can continue
        }

        // Use supervisorScope so failures in one repository don't cancel others
        supervisorScope {
            launch(exceptionHandler) {
                try {
                    transactionRepository.refreshData()
                } catch (e: Exception) {
                    // Individual error handling - this refresh failed but others continue
                }
            }
            
            launch(exceptionHandler) {
                try {
                    appConfigRepository.refreshData()
                } catch (e: Exception) {
                    // Individual error handling - this refresh failed but others continue
                }
            }
            
            launch(exceptionHandler) {
                try {
                    congregationRepository.refreshData()
                } catch (e: Exception) {
                    // Individual error handling - this refresh failed but others continue
                }
            }
            
            launch(exceptionHandler) {
                try {
                    asrExpenseRepository.refreshData()
                } catch (e: Exception) {
                    // Individual error handling - this refresh failed but others continue
                }
            }

            launch(exceptionHandler) {
                try {
                    decontRepository.refreshData()
                } catch (e: Exception) {
                    // Individual error handling - this refresh failed but others continue
                }
            }

            launch(exceptionHandler) {
                try {
                    currentAsrSituationRepository.refreshData()
                } catch (e: Exception) {
                    // Individual error handling - this refresh failed but others continue
                    // CurrentAsrSituation is non-critical, so we silently handle errors
                }
            }
        }
    }
}
