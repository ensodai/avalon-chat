package app.domain.chat.usecases
import org.koin.core.annotation.Single

import app.service.TokenService
import io.ktor.server.websocket.*
import kotlin.uuid.Uuid

@Single
class AuthenticateSessionUseCase(
        private val tokenService: TokenService
) {
    // Используем operator fun invoke, чтобы вызывать класс как функцию: useCase(session, token)
    operator fun invoke(session: DefaultWebSocketServerSession, token: String): Result<Uuid> {
        val userId = tokenService.getUserIdFromToken(token)

        return if (userId != null) {
//            connectionManager.authenticate(session, userId)
            Result.success(userId)
        } else {
            Result.failure(Exception("Invalid token"))
        }
    }
}