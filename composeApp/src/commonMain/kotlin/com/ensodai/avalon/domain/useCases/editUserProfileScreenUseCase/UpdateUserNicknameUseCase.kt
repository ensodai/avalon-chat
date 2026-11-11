package com.ensodai.avalon.domain.useCases.editUserProfileScreenUseCase

import com.ensodai.avalon.domain.repository.local.LocalUserRepository
import com.ensodai.shared.avalon.user.models.UpdateNicknameRequest
import com.ensodai.shared.avalon.rpc.services.UserRpcService
import org.koin.core.annotation.Single

@Single
class UpdateUserNicknameUseCase(
    private val userRpcService: UserRpcService,
    private val localUserRepository: LocalUserRepository
) {

    suspend fun update(nickName: String): Result<Unit> {
        return runCatching {
            userRpcService.updateUserNickname(UpdateNicknameRequest(nickname = nickName))
        }.onSuccess { localUserRepository.updateNickname(nickName) }
    }
}