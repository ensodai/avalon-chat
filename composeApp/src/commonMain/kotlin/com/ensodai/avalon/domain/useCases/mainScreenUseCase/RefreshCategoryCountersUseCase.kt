package com.ensodai.avalon.domain.useCases.mainScreenUseCase

import com.ensodai.shared.avalon.rpc.services.CategoryRpcService
import com.ensodai.avalon.data.db.LocaleHelper
import com.ensodai.avalon.data.db.cache.toServerCategoryEntity
import com.ensodai.avalon.domain.repository.local.AppSettingsRepository
import com.ensodai.avalon.domain.repository.local.LocalCategoryRepository
import org.koin.core.annotation.Single

@Single
class RefreshCategoryCountersUseCase(
    private val categoryRpcService: CategoryRpcService,
    private val localCategoryRepository: LocalCategoryRepository,
    private val appSettingsRepository: AppSettingsRepository
) {

    suspend fun refresh(): Result<Unit> = runCatching {
        val response = categoryRpcService.getCategoryCounts()
        val localeCode = appSettingsRepository.getSavedLanguage() ?: "en"
        val serverCategoryEntities = response.map {
            it.toServerCategoryEntity(localeCode)
        }
        localCategoryRepository.updateServerCategories(serverCategoryEntities)
    }
}