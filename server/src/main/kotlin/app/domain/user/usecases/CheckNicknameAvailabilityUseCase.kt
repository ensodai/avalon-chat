package app.domain.user.usecases

import org.koin.core.annotation.Factory
import com.ensodai.shared.avalon.user.models.AvailableResultResponse
import app.data.repository.UserRepositoryApp

@Factory
class CheckNicknameAvailabilityUseCase(private val userRepository: UserRepositoryApp) {
    suspend fun execute(nickname: String): AvailableResultResponse {
        return try {
            val isAvailable = userRepository.isNicknameAvailable(nickname)
            AvailableResultResponse(isAvailable)
        } catch (e: Exception) {
            throw Exception(e.message ?: "Failed to check availability")
        }
    }
}
