package com.ensodai.avalon.domain.useCases.customizationScreenUseCase

import com.ensodai.avalon.domain.repository.local.AppSettingsRepository
import com.ensodai.avalon.theme.models.AvalonThemePreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
class GetSelectedThemePresetUseCase(
    private val appSettingsRepository: AppSettingsRepository
) {
    operator fun invoke(): Flow<AvalonThemePreset> {
        return appSettingsRepository.getAvalonColorsFlow().map { it.preset }
    }
}
