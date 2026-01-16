package com.asr.financial.data.repository

import com.asr.financial.data.datasource.CongregationDataSource
import com.asr.financial.domain.models.CongregationInfo
import com.asr.financial.domain.repository.CongregationInfoRepository

/**
 * Implementation of CongregationInfoRepository.
 * Delegates to the injected DataSource.
 */
class CongregationInfoRepositoryImpl(
    private val dataSource: CongregationDataSource
) : CongregationInfoRepository {

    override suspend fun getAll(): List<CongregationInfo> {
        return dataSource.getAll()
    }

    override suspend fun getActive(): List<CongregationInfo> {
        return dataSource.getActive()
    }

    override suspend fun getById(id: String): CongregationInfo? {
        return dataSource.getById(id)
    }

    override suspend fun getAllNames(): List<String> {
        return dataSource.getAllNames()
    }

    override suspend fun getCount(): Int {
        return dataSource.getCount()
    }

    override suspend fun getTotalMemberCount(): Int {
        return dataSource.getTotalMemberCount()
    }
}
