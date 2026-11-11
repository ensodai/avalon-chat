package app.domain.user.usecases

import app.data.repository.UserRepositoryApp
import com.ensodai.shared.avalon.user.models.UpdateUserFlowModeRequest
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
class UpdateUserFlowModeUseCase(
    private val userRepository: UserRepositoryApp
) {
    suspend fun execute(request: UpdateUserFlowModeRequest, userId: Uuid) {
        val success = userRepository.updateFlowModeSettings(
            userId = userId,
            isFlowModeEnabled = request.isFlowModeEnabled
        )
        if (!success) {
            throw Exception("Failed to update flow mode settings")
        }
    }
}
