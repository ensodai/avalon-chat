package com.ensodai.avalon.data.repository.local

import com.ensodai.avalon.data.repository.local.base.BaseRepository
import com.ensodai.avalon.data.repository.local.base.DatabaseExecutor

import com.ensodai.avalon.data.db.cache.AvailableLlmConfigs
import com.ensodai.avalon.data.db.dao.CacheDao
import com.ensodai.shared.avalon.sync.models.LlmConfigResponse
import com.ensodai.avalon.domain.repository.local.LocalCacheRepository

class LocalCacheRepositoryImpl(
    dbExecutor: DatabaseExecutor,
    private val cacheDao: CacheDao
) : BaseRepository(dbExecutor), LocalCacheRepository {

    override suspend fun updateAvailableLlmConfigs(configs: List<LlmConfigResponse>) {

        val entities = configs.map { response ->
            AvailableLlmConfigs(
                codeName = response.codeName,
                displayName = response.displayName,
                price = response.price
            )
        }

        dbWrite { cacheDao.updateConfigsTransaction(entities) }
    }

    override suspend fun getAvailableLlmConfigs(): List<AvailableLlmConfigs> {
        return dbRead { cacheDao.getAvailableLlmConfigs() }
    }
}