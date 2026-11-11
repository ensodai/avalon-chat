package com.ensodai.avalon.domain.useCases.userAccountScreenUseCase

import com.ensodai.shared.avalon.rpc.services.AuthRpcService
import com.ensodai.shared.avalon.auth.models.LogoutRequest
import com.ensodai.avalon.data.repository.local.DatastoreManager
import org.koin.core.annotation.Single

@Single
class DeleteAccountUseCase(
    private val authRpcService: AuthRpcService,
    private val datastoreManager: DatastoreManager
) {

    suspend fun delete() {
        runCatching { authRpcService.deleteAccount() }
        val refreshToken = datastoreManager.getRefreshToken() ?: ""
        runCatching { authRpcService.logout(LogoutRequest(refreshToken)) }
    }
}
