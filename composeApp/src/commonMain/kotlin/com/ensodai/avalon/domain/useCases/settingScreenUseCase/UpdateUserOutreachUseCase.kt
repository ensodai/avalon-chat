package com.ensodai.avalon.domain.useCases.settingScreenUseCase

import com.ensodai.shared.avalon.rpc.services.UserRpcService
import com.ensodai.shared.avalon.user.models.UpdateUserOutreachRequest
import com.ensodai.avalon.domain.repository.local.LocalUserRepository
import org.koin.core.annotation.Single

@Single
class UpdateUserOutreachUseCase(
    private val userRpcService: UserRpcService,
    private val localUserRepository: LocalUserRepository
) {
    suspend fun execute(isOutreachEnabled: Boolean, dailyOutreachLimit: Int) {
        // Обновляем локально, чтобы изменения отразились мгновенно
        localUserRepository.updateUserOutreachSettings(isOutreachEnabled, dailyOutreachLimit)
        
        // Отправляем запрос на бэкенд
        userRpcService.updateUserOutreach(
            UpdateUserOutreachRequest(
                isOutreachEnabled = isOutreachEnabled,
                dailyOutreachLimit = dailyOutreachLimit
            )
        )
    }
}
