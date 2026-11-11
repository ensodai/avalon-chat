package app.application.chat

import app.data.db.tables.LlmConfig
import app.data.models.AICharacterConfigLLMDto

data class PreparedSendMessageContext(
    val actor: ChatActor,
    val command: ChatCommand.SendMessage,
    val chatContext: ChatCommandContext,
    val llmConfig: LlmConfig,
    val characterConfig: AICharacterConfigLLMDto
)
