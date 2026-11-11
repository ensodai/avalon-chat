package com.ensodai.avalon.domain.useCases.commonUseCase.promoFlags

import com.ensodai.avalon.data.db.models.ShowPromoFlagsEntity
import com.ensodai.avalon.domain.repository.local.AppSettingsRepository
import org.koin.core.annotation.Single

@Single
class DisableOpenApiTransitionAnnouncementPromoFlagUseCase(
    private val appSettingsRepository: AppSettingsRepository
) {

    suspend fun disable() {
        val flags = appSettingsRepository.getShowPromoFlags() ?: ShowPromoFlagsEntity()
        appSettingsRepository.updateShowPromoFlags(flags.copy(openApiTransitionAnnouncement = false))
    }
}
