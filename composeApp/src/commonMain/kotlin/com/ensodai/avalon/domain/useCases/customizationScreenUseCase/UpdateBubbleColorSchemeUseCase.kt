package com.ensodai.avalon.domain.useCases.customizationScreenUseCase

import com.ensodai.avalon.domain.repository.local.AppSettingsRepository
import com.ensodai.avalon.theme.models.BubbleColorScheme
import com.ensodai.avalon.theme.toHex
import org.koin.core.annotation.Factory

@Factory
class UpdateBubbleColorSchemeUseCase(
    private val appSettingsRepository: AppSettingsRepository
) {
    /**
     * Применяет цветовую схему бабблов.
     * null = сброс на дефолт темы.
     */
    suspend operator fun invoke(scheme: BubbleColorScheme?) {
        if (scheme == null) {
            appSettingsRepository.resetBubbleColorOverride()
        } else {
            appSettingsRepository.updateChatBubbleColors(userColorHex = scheme.outgoingColor.toHex())
        }
    }
}
