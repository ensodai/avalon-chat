package com.ensodai.avalon.domain.useCases.commonUseCase.promoFlags

import com.ensodai.avalon.data.db.models.ShowPromoFlagsEntity
import com.ensodai.avalon.domain.repository.local.AppSettingsRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class GetShowPromoFlagsFlowUseCase(
    private val appSettingsRepository: AppSettingsRepository
) {

    fun get(): Flow<ShowPromoFlagsEntity?> {
        return appSettingsRepository.getShowPromoFlagsFlow()
    }

}