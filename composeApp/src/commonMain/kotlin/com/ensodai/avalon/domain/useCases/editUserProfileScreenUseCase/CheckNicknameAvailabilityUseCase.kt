package com.ensodai.avalon.domain.useCases.editUserProfileScreenUseCase

import com.ensodai.shared.avalon.user.models.AvailableResultResponse
import com.ensodai.shared.avalon.user.models.CheckNicknameRequest
import com.ensodai.shared.avalon.rpc.services.UserRpcService
import org.koin.core.annotation.Single

@Single
class CheckNicknameAvailabilityUseCase(
    private val userRpcService: UserRpcService
) {

    suspend fun check(nickName: String): Result<AvailableResultResponse> {
        return runCatching {
            userRpcService.checkNicknameAvailability(CheckNicknameRequest(nickname = nickName))
        }
    }
}