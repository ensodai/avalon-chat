package app.application.chat

import app.data.db.tables.chatSessions.ChatSessionInternalDto
import app.data.db.tables.chatSessions.ChatVariantInternalDto
import app.data.repository.ChatRepository
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single

data class ChatCommandContext(
    val actor: ChatActor,
    val command: ChatCommand,
    val session: ChatSessionInternalDto?,
    val variant: ChatVariantInternalDto?,
    val mode: ChatContextMode
)

sealed interface ChatContextLoadResult {
    data class Success(
        val context: ChatCommandContext
    ) : ChatContextLoadResult

    data class Failure(
        val reason: String
    ) : ChatContextLoadResult
}

@Single
class ChatContextLoader(
    private val chatRepository: ChatRepository
) {

    suspend fun load(
        actor: ChatActor,
        command: ChatCommand
    ): ChatContextLoadResult {
        val policy = command.policy()
        val session = chatRepository.findSessionByIdAndUser(
            sessionId = command.chatSessionId,
            userId = actor.userId
        )
        val variant = chatRepository.findVariantByIdAndUser(
            variantId = command.chatVariantId,
            userId = actor.userId
        )

        if (variant != null && variant.chatSessionId != command.chatSessionId) {
            return ChatContextLoadResult.Failure("Chat variant does not belong to chat session")
        }

        if (session != null && variant != null && variant.chatSessionId != session.id) {
            return ChatContextLoadResult.Failure("Chat session and variant mismatch")
        }

        val expectedCharacterId = command.characterIdOrNull()
        if (policy.requiresCharacterMatch && expectedCharacterId == null) {
            return ChatContextLoadResult.Failure("Character is required for this command")
        }

        if (expectedCharacterId != null && session != null && session.characterId != expectedCharacterId) {
            return ChatContextLoadResult.Failure("Chat session does not belong to requested character")
        }

        return when (policy.contextMode) {
            ChatContextMode.EXISTING_CHAT_REQUIRED -> loadExistingContext(
                actor = actor,
                command = command,
                session = session,
                variant = variant
            )

            ChatContextMode.BOOTSTRAP_ALLOWED -> loadBootstrapAwareContext(
                actor = actor,
                command = command,
                session = session,
                variant = variant
            )
        }
    }

    private fun loadExistingContext(
        actor: ChatActor,
        command: ChatCommand,
        session: ChatSessionInternalDto?,
        variant: ChatVariantInternalDto?
    ): ChatContextLoadResult {
        if (session == null) {
            return ChatContextLoadResult.Failure("Chat session not found")
        }
        if (variant == null) {
            return ChatContextLoadResult.Failure("Chat variant not found")
        }

        return ChatContextLoadResult.Success(
            ChatCommandContext(
                actor = actor,
                command = command,
                session = session,
                variant = variant,
                mode = ChatContextMode.EXISTING_CHAT_REQUIRED
            )
        )
    }

    private fun loadBootstrapAwareContext(
        actor: ChatActor,
        command: ChatCommand,
        session: ChatSessionInternalDto?,
        variant: ChatVariantInternalDto?
    ): ChatContextLoadResult {
        if (variant != null && session == null) {
            return ChatContextLoadResult.Failure("Chat variant exists without accessible chat session")
        }

        return ChatContextLoadResult.Success(
            ChatCommandContext(
                actor = actor,
                command = command,
                session = session,
                variant = variant,
                mode = if (session == null || variant == null) {
                    ChatContextMode.BOOTSTRAP_ALLOWED
                } else {
                    ChatContextMode.EXISTING_CHAT_REQUIRED
                }
            )
        )
    }
}
