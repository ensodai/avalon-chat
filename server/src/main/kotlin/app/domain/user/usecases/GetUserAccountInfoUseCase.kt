package app.domain.user.usecases

import app.data.repository.UserRepositoryApp
import com.ensodai.shared.avalon.user.models.UserAccountInfoResponse
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
class GetUserAccountInfoUseCase(private val userRepository: UserRepositoryApp) {
    suspend fun execute(userId: Uuid): UserAccountInfoResponse {
        return try {
            userRepository.getUserAccountInfo(userId)
                ?: throw Exception("User not found")
        } catch (e: Exception) {
            throw Exception(e.message ?: "Failed to get user info")
        }
    }
}
