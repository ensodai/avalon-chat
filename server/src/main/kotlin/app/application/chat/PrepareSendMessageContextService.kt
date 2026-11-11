package app.application.chat

import app.data.repository.UserRepository
import app.service.LlmConfigService
import app.domain.chat.ChatContextService
import org.koin.core.annotation.Single

@Single
class PrepareSendMessageContextService(
    private val llmConfigService: LlmConfigService,
    private val chatContextService: ChatContextService,
    private val userRepository: UserRepository
) {

    suspend fun prepare(
        context: ChatCommandContext,
        command: ChatCommand.SendMessage
    ): PreparedSendMessageContext? {
        val userPremiumStatus = userRepository.getUserPremiumStatus(context.actor.userId)
        val llmConfig = llmConfigService.getConfig(command.llmModelCode, userPremiumStatus)
        val characterConfig = chatContextService.getOrCreateContext(
            userId = context.actor.userId,
            chatSessionId = command.chatSessionId,
            chatVariantId = command.chatVariantId,
            characterId = command.characterId,
            llmConfig = llmConfig
        ) ?: return null

        return PreparedSendMessageContext(
            actor = context.actor,
            command = command,
            chatContext = context,
            llmConfig = llmConfig,
            characterConfig = characterConfig
        )
    }
}
