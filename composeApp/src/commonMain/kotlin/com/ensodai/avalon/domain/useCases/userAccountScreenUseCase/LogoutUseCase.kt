package com.ensodai.avalon.domain.useCases.userAccountScreenUseCase

import com.ensodai.avalon.data.repository.local.DatastoreManager
import com.ensodai.avalon.domain.auth.AuthSessionManager
import com.ensodai.avalon.domain.image.ImageCacheManager
import com.ensodai.shared.avalon.rpc.services.AuthRpcService
import com.ensodai.shared.avalon.auth.models.LogoutRequest
import com.ensodai.avalon.domain.chatEvents.ChatSyncRpcClient
import com.ensodai.avalon.utils.AppLogger
import com.ensodai.avalon.utils.IODispatcher
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single

@Single
class LogoutUseCase(
    private val authSessionManager: AuthSessionManager,
    private val authRpcService: AuthRpcService,
    private val chatSyncRpcClient: ChatSyncRpcClient,
    private val imageCacheManager: ImageCacheManager,
    private val datastoreManager: DatastoreManager,
    private val logger: AppLogger
) {

    suspend fun logout() {
        logger.i("LogoutUseCase", "🛑 UI Initiated Logout")

        val refreshToken = datastoreManager.getRefreshToken() ?: ""

        // Убиваем сокет
        chatSyncRpcClient.cleanup()

        // Запускаем неотменяемую операцию очистки
        withContext(IODispatcher + NonCancellable) {
            runCatching { authRpcService.logout(LogoutRequest(refreshToken)) }
        }

        withContext(IODispatcher) {

            try {
                imageCacheManager.clearAll()
                logger.d("LogoutUseCase", "🧹 Image cache cleared")
            } catch (e: Exception) {
                logger.e("LogoutUseCase", "❌ Image loader clear failed: ${e.stackTraceToString()}")
            }
        }

        authSessionManager.logoutLocally()

        logger.i("LogoutUseCase", "✅ Logout finished.")
    }

    suspend fun delete() {
        logger.i("LogoutUseCase", "🛑 Initiating Ultimate Logout...")

        chatSyncRpcClient.cleanup()
        
        runCatching { authRpcService.deleteAccount() }

        withContext(IODispatcher) {

            try {
                imageCacheManager.clearAll()
                logger.d("LogoutUseCase", "🧹 Image cache cleared")
            } catch (e: Exception) {
                logger.e("LogoutUseCase", "❌ Image loader clear failed: ${e.stackTraceToString()}")
            }
        }
        
        val refreshToken = datastoreManager.getRefreshToken() ?: ""
        runCatching { authRpcService.logout(LogoutRequest(refreshToken)) }
        
        authSessionManager.logoutLocally()
        logger.i("LogoutUseCase", "✅ Logout sequence finished.")
    }

}
