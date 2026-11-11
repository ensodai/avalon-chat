package com.ensodai.avalon.domain.useCases.customizationScreenUseCase

import com.ensodai.avalon.domain.repository.local.AppSettingsRepository
import com.ensodai.avalon.theme.models.ChatRoleplayPreset
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class GetChatRoleplayPresetFlowUseCase(
    private val appSettingsRepository: AppSettingsRepository
) {
    operator fun invoke(): Flow<ChatRoleplayPreset> {
        return appSettingsRepository.getChatRoleplayPresetFlow()
    }
}
