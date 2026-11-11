package app.domain.chat.usecases

import org.koin.core.annotation.Factory
import app.data.repository.ChatRepository
import kotlin.uuid.Uuid

@Factory
class DeleteChatVariantUseCase(
    private val chatRepository: ChatRepository
) {
    suspend fun execute(variantId: Uuid, userId: Uuid) {
        val hasAccess = chatRepository.verifyVariantOwnership(variantId, userId)
        if (!hasAccess) {
            throw Exception("Chat variant not found or access denied")
        }
        chatRepository.deleteVariant(variantId)
    }
}
