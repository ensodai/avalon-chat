package app.domain.auth.usecases

import org.koin.core.annotation.Factory
import app.data.repository.UserRepositoryApp
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Factory
class DeleteUserAccountUseCase(private val userRepository: UserRepositoryApp) {
    suspend fun execute(userId: Uuid) {
        userRepository.markAsDeleted(userId)
    }
}
