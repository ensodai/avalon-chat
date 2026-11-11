package app.domain.user.usecases

import app.data.repository.UserRepositoryApp
import com.ensodai.shared.avalon.user.models.UpdateUserOutreachRequest
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
class UpdateUserOutreachUseCase(
    private val userRepository: UserRepositoryApp
) {
    suspend fun execute(request: UpdateUserOutreachRequest, userId: Uuid) {
        val success = userRepository.updateOutreachSettings(
            userId = userId,
            isOutreachEnabled = request.isOutreachEnabled,
            dailyOutreachLimit = request.dailyOutreachLimit
        )
        if (!success) {
            throw Exception("Failed to update outreach settings")
        }
    }
}
