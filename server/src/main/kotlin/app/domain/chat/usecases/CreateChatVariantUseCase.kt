package app.domain.chat.usecases

import org.koin.core.annotation.Factory
import app.data.repository.ChatRepository
import com.ensodai.shared.avalon.chat.models.ChatVariantResponse
import app.apiv4.mappers.toSharedChatVariant
import kotlin.uuid.Uuid

@Factory
class CreateChatVariantUseCase(
    private val chatRepository: ChatRepository
) {
    suspend fun execute(sessionId: Uuid, userId: Uuid, chatVariantId: Uuid): ChatVariantResponse {
        val hasAccess = chatRepository.verifySessionOwnership(sessionId, userId)

        if (!hasAccess) {
            throw Exception("Chat session not found or access denied for user $userId")
        }

        val result = chatRepository.createNewVariant(
            sessionId = sessionId,
            chatVariantId = chatVariantId
        )
        
        return result.toSharedChatVariant()
    }
}
