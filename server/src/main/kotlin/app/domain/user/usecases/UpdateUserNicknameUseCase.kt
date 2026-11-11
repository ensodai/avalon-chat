package app.domain.user.usecases

import org.koin.core.annotation.Factory
import com.ensodai.shared.avalon.user.models.UpdateNicknameRequest
import app.data.repository.UserRepositoryApp
import app.domain.models.NicknameUpdateResult
import app.service.NicknameValidator
import kotlin.uuid.Uuid

@Factory
class UpdateUserNicknameUseCase(
    private val userRepository: UserRepositoryApp,
    private val nicknameValidator: NicknameValidator
) {
    suspend fun execute(request: UpdateNicknameRequest, userId: Uuid) {
        val nickname = request.nickname

        val validationResult = nicknameValidator.validate(nickname)
        if (validationResult !is NicknameValidator.ValidationResult.Valid) {
            throw Exception(validationResult.message)
        }

        try {
            when (userRepository.updateNickname(userId, nickname)) {
                is NicknameUpdateResult.Success -> return
                is NicknameUpdateResult.NicknameTaken -> throw Exception("This nickname is already taken")
                is NicknameUpdateResult.UserNotFound -> throw Exception("User not found")
            }
        } catch (e: Exception) {
            if (e.message?.contains("already taken") == true || e.message?.contains("not found") == true) throw e
            throw Exception(e.message ?: "Failed to update nickname")
        }
    }
}
