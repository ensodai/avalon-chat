package app.domain.auth.usecases

import org.koin.core.annotation.Factory
import app.data.repository.SessionRepository

@Factory
class LogoutUseCase(private val sessionRepository: SessionRepository) {
    suspend fun execute(refreshToken: String) {
        if (refreshToken.isBlank()) {
            throw Exception("Refresh token is required.")
        }
        
        val success = runCatching {
            sessionRepository.revokeSessionByRefreshToken(refreshToken)
        }.getOrDefault(false)
        
        if (!success) {
            throw Exception("Refresh token not found.")
        }
    }
}
