package com.ensodai.avalon.domain.useCases.customizationScreenUseCase

import com.ensodai.avalon.domain.repository.local.AppSettingsRepository
import com.ensodai.avalon.theme.AvalonColors
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class GetAvalonColorsFlowUseCase(
    private val appSettingsRepository: AppSettingsRepository
) {
    operator fun invoke(): Flow<AvalonColors> {
        return appSettingsRepository.getAvalonColorsFlow()
    }
}
