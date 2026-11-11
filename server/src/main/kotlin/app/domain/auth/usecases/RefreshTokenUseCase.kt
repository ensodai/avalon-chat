package app.domain.auth.usecases

import org.koin.core.annotation.Factory
import com.ensodai.shared.avalon.auth.models.AuthResponse
import app.data.repository.RotationResult
import app.data.repository.SessionRepository
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
@Factory
class RefreshTokenUseCase(private val sessionRepository: SessionRepository) {
    suspend fun execute(refreshToken: String, fcmToken: String? = null): AuthResponse {
        val result = sessionRepository.rotateRefreshToken(refreshToken, fcmToken)
        
        return when (result) {
            is RotationResult.Success -> {
                AuthResponse(
                    accessToken = result.accessToken,
                    refreshToken = result.refreshToken,
                    userId = result.userId
                )
            }
            is RotationResult.Revoked -> throw Exception("Session revoked or expired")
            is RotationResult.NotFound -> throw Exception("Invalid refresh token")
            is RotationResult.TheftDetected -> throw Exception("Security Warning: Reuse of revoked token detected. Session terminated.")
        }
    }
}
