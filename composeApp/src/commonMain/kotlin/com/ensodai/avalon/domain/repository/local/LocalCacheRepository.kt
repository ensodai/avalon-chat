package com.ensodai.avalon.domain.repository.local

import com.ensodai.avalon.data.db.cache.AvailableLlmConfigs
import com.ensodai.shared.avalon.sync.models.LlmConfigResponse

interface LocalCacheRepository {

    suspend fun updateAvailableLlmConfigs(configs: List<LlmConfigResponse>)

    suspend fun getAvailableLlmConfigs(): List<AvailableLlmConfigs>

}