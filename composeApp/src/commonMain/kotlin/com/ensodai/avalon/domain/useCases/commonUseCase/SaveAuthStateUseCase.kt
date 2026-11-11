package com.ensodai.avalon.domain.useCases.commonUseCase

import com.ensodai.avalon.domain.repository.local.AppSettingsRepository
import org.koin.core.annotation.Single

@Single
class SaveAuthStateUseCase(appSettingsRepository: AppSettingsRepository) {

    fun save() {

    }
}