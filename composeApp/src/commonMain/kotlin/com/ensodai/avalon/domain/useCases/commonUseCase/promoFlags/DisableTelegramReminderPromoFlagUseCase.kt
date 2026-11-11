package com.ensodai.avalon.domain.useCases.commonUseCase.promoFlags

import com.ensodai.avalon.domain.repository.local.AppSettingsRepository
import org.koin.core.annotation.Single

@Single
class DisableTelegramReminderPromoFlagUseCase(
    private val appSettingsRepository: AppSettingsRepository
) {

    suspend fun disable() {
        val flags = appSettingsRepository.getShowPromoFlags()
        flags?.copy(telegramReminder = false)
            ?.let { appSettingsRepository.updateShowPromoFlags(it) }

    }
}
