package com.ensodai.avalon.data.repository.local

import com.ensodai.avalon.data.repository.local.base.BaseRepository
import com.ensodai.avalon.data.repository.local.base.DatabaseExecutor


import com.ensodai.avalon.data.db.dao.MainRulesDao
import com.ensodai.avalon.data.db.models.MainRulesEntity
import com.ensodai.avalon.domain.repository.remote.MainRulesRepository

class MainRulesRepositoryImpl(
    dbExecutor: DatabaseExecutor,
    private val mainRulesDao: MainRulesDao
) : BaseRepository(dbExecutor), MainRulesRepository {

    override suspend fun getMainRules(): MainRulesEntity? {
        return dbRead { mainRulesDao.getMainRules() }
    }

}