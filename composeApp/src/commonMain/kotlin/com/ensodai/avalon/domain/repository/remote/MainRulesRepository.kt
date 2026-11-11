package com.ensodai.avalon.domain.repository.remote

import com.ensodai.avalon.data.db.models.MainRulesEntity

interface MainRulesRepository {

    suspend fun getMainRules(): MainRulesEntity?

}