package com.asr.financial.domain.usecase

import com.asr.financial.domain.models.CongregationInfo
import com.asr.financial.domain.repository.CongregationInfoRepository

/**
 * UseCase for getting all congregations
 */
class GetAllCongregationsUseCase(
    private val repository: CongregationInfoRepository
) {
    suspend operator fun invoke(): List<CongregationInfo> {
        return repository.getAll()
    }
}

/**
 * UseCase for getting all congregation names
 */
class GetCongregationNamesUseCase(
    private val repository: CongregationInfoRepository
) {
    suspend operator fun invoke(): List<String> {
        return repository.getAllNames()
    }
}

/**
 * UseCase for getting congregation count
 */
class GetCongregationCountUseCase(
    private val repository: CongregationInfoRepository
) {
    suspend operator fun invoke(): Int {
        return repository.getCount()
    }
}

/**
 * UseCase for getting total member/publisher count
 */
class GetTotalMemberCountUseCase(
    private val repository: CongregationInfoRepository
) {
    suspend operator fun invoke(): Int {
        return repository.getTotalMemberCount()
    }
}
