package com.asr.financial.data.repository

import com.asr.financial.data.cache.TimeBasedRepositoryCache
import com.asr.financial.data.datasource.SituatieCurentaAsrDataSource
import com.asr.financial.domain.models.SituatieCurentaAsr
import com.asr.financial.domain.repository.CurrentAsrSituationRepository
import com.asr.financial.platform.Clock
import com.asr.financial.platform.Logger

/**
 * Implementation of CurrentAsrSituationRepository with time-based caching (12 hours).
 * Uses cache-first strategy: checks cache first, loads from server if cache is empty or expired.
 */
class CurrentAsrSituationRepositoryImpl(
    private val dataSource: SituatieCurentaAsrDataSource,
    private val clock: Clock,
    private val logger: Logger
) : CurrentAsrSituationRepository {

    private companion object {
        const val TAG = "SituatieCurentaRepo"
    }

    private val cache = TimeBasedRepositoryCache<SituatieCurentaAsr>(clock)

    override suspend fun get(): SituatieCurentaAsr? {
        // Cache-first strategy: check cache first
        val cached = cache.get()
        if (cached != null) {
            logger.debug(TAG, "situatie_curenta_asr: cache HIT")
            return cached
        }

        logger.debug(TAG, "situatie_curenta_asr: cache MISS, loading from Cloud")
        // Cache is empty or expired, load from server
        val currentSituation = dataSource.get()
        if (currentSituation == null) {
            logger.warning(TAG, "situatie_curenta_asr: dataSource.get() returned null (see SituatieCurentaAsrDS logs for download/parse details)")
        } else {
            logger.debug(TAG, "situatie_curenta_asr: loaded from Cloud, caching (year=${currentSituation.year}, month=${currentSituation.month})")
            cache.set(currentSituation)
        }
        return currentSituation
    }

    override suspend fun refreshData() {
        // Clear cache and force reload from server
        cache.clear()
        val currentSituation = dataSource.get()
        if (currentSituation != null) {
            cache.set(currentSituation)
        }
    }
}
