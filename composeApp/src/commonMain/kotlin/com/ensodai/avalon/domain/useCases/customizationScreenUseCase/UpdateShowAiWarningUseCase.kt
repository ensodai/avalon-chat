package com.ensodai.avalon.domain.useCases.customizationScreenUseCase

import com.ensodai.avalon.domain.repository.local.AppSettingsRepository
import org.koin.core.annotation.Factory

@Factory
class UpdateShowAiWarningUseCase(
    private val appSettingsRepository: AppSettingsRepository
) {
    suspend operator fun invoke(isShow: Boolean) {
        appSettingsRepository.changeShowAiWarning(isShow)
    }
}
