package com.ensodai.avalon.domain.useCases.customizationScreenUseCase

import com.ensodai.avalon.domain.repository.local.AppSettingsRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class GetShowAiWarningFlowUseCase(
    private val appSettingsRepository: AppSettingsRepository
) {
    operator fun invoke(): Flow<Boolean> {
        return appSettingsRepository.getShowAiWarningFlow()
    }
}
