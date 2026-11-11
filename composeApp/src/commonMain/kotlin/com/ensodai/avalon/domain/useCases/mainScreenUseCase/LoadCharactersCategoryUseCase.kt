package com.ensodai.avalon.domain.useCases.mainScreenUseCase

import com.ensodai.shared.avalon.rpc.services.CategoryRpcService
import com.ensodai.avalon.data.db.LocaleHelper
import com.ensodai.avalon.data.db.cache.toCategoryEntities
import com.ensodai.avalon.data.db.cache.toServerCategoryEntities
import com.ensodai.avalon.domain.repository.local.AppSettingsRepository
import com.ensodai.avalon.domain.repository.local.LocalCategoryRepository
import org.koin.core.annotation.Single

@Single
class LoadCharactersCategoryUseCase(
    private val categoryRpcService: CategoryRpcService,
    private val localCategoryRepository: LocalCategoryRepository,
    private val appSettingsRepository: AppSettingsRepository
) {

    suspend fun load(): Result<Unit> = runCatching {
        val response = categoryRpcService.getCombinedCategories()
        val localeCode = appSettingsRepository.getSavedLanguage() ?: "en"
        
        val selectableCategoryEntities = response.selectableCategories.toCategoryEntities(localeCode)
        val serverCategoryEntities = response.serverCategories.toServerCategoryEntities(localeCode)

        localCategoryRepository.saveCategories(
            selectableCategories = selectableCategoryEntities,
            serverCategories = serverCategoryEntities
        )
    }
}