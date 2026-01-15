package com.asr.financial.domain.repository

import com.asr.financial.domain.models.Donation
import com.asr.financial.domain.models.FinancialPeriod
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Donation operations
 * Implementation in data layer
 */
interface DonationRepository {
    fun getAllDonations(): Flow<List<Donation>>
    fun getDonationsByPeriod(period: FinancialPeriod): Flow<List<Donation>>
    fun getDonationsByCongregation(congregationId: Long): Flow<List<Donation>>
    suspend fun getDonationById(id: Long): Donation?
    suspend fun getTotalByPeriod(period: FinancialPeriod): Double
    suspend fun insertDonation(donation: Donation): Long
    suspend fun updateDonation(donation: Donation)
    suspend fun deleteDonation(id: Long)
}
