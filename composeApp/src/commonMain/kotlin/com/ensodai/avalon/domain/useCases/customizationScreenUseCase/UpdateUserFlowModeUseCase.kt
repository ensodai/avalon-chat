package com.ensodai.avalon.domain.useCases.customizationScreenUseCase

import com.ensodai.shared.avalon.rpc.services.UserRpcService
import com.ensodai.shared.avalon.user.models.UpdateUserFlowModeRequest
import com.ensodai.avalon.domain.repository.local.LocalUserRepository
import org.koin.core.annotation.Single

@Single
class UpdateUserFlowModeUseCase(
    private val userRpcService: UserRpcService,
    private val localUserRepository: LocalUserRepository
) {
    suspend operator fun invoke(isEnabled: Boolean) {
        // Optimistic update locally
        localUserRepository.updateUserFlowMode(isEnabled)
        try {
            userRpcService.updateUserFlowMode(UpdateUserFlowModeRequest(isEnabled))
        } catch (e: Exception) {
            // Revert locally on error
            localUserRepository.updateUserFlowMode(!isEnabled)
            throw e
        }
    }
}
