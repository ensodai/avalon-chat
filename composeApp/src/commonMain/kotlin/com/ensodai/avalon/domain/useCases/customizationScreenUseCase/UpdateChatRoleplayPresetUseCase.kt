package com.ensodai.avalon.domain.useCases.customizationScreenUseCase

import com.ensodai.avalon.domain.repository.local.AppSettingsRepository
import com.ensodai.avalon.theme.models.ChatRoleplayPreset
import org.koin.core.annotation.Factory

@Factory
class UpdateChatRoleplayPresetUseCase(
    private val appSettingsRepository: AppSettingsRepository
) {
    suspend operator fun invoke(preset: ChatRoleplayPreset) {
        appSettingsRepository.updateChatRoleplayPreset(preset)
    }
}
