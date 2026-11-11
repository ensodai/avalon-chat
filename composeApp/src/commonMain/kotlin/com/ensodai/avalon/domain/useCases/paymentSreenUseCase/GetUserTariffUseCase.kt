package com.ensodai.avalon.domain.useCases.paymentSreenUseCase

import com.ensodai.avalon.data.db.dto.UserTariffInfoDto
import com.ensodai.avalon.domain.repository.local.LocalUserRepository
import org.koin.core.annotation.Single

@Single
class GetUserTariffUseCase(
    private val repository: LocalUserRepository
) {

    suspend fun get(): UserTariffInfoDto? {
        return repository.getUserTariff()
    }

}