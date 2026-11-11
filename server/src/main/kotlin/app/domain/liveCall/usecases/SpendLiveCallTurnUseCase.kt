package app.domain.liveCall.usecases

import app.data.db.tables.LlmConfig
import app.data.models.EnergyDeductionResult
import app.data.repository.UserRepository
import kotlin.uuid.Uuid
import org.koin.core.annotation.Factory

data class SpendLiveCallTurnCommand(
    val userId: Uuid,
    val chatVariantId: Uuid,
    val llmConfig: LlmConfig,
    val resolvedModel: String
)

@Factory
class SpendLiveCallTurnUseCase(
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(command: SpendLiveCallTurnCommand): EnergyDeductionResult {
        return userRepository.deductLiveCallTurn(
            userId = command.userId,
            llmConfig = command.llmConfig,
            metaInfo = mapOf(
                "chat_variant_id" to command.chatVariantId.toString(),
                "live_model" to command.resolvedModel
            )
        )
    }
}
