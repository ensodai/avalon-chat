package com.ensodai.avalon.domain.useCases.userAiSettingsScreenUseCase

import com.ensodai.shared.avalon.user.models.AiModelCategory
import com.ensodai.shared.avalon.user.models.AiModelCatalogItem
import com.ensodai.shared.avalon.user.models.AiProviderType
import com.ensodai.shared.avalon.user.models.GetAiCatalogModelsRequest
import com.ensodai.avalon.domain.repository.remote.UserAiSettingsRepository
import org.koin.core.annotation.Factory

@Factory
class GetAvailableAiModelsUseCase(
    private val repository: UserAiSettingsRepository
) {
    suspend fun execute(
        provider: AiProviderType,
        category: AiModelCategory
    ): List<AiModelCatalogItem> {
        val req = GetAiCatalogModelsRequest(provider = provider, category = category)
        return repository.getAvailableAiModels(req).models
    }
}
