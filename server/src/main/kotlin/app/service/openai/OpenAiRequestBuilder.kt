package app.service.openai

import app.data.models.AICharacterConfigLLMDto
import app.data.network.models.openai.OpenAiChatMessage
import app.data.network.models.openai.OpenAiChatRequest
import app.data.network.models.openai.OpenAiStreamOptions
import app.domain.models.DomainMessage
import app.service.GeminiRequestFactory
import app.service.chat.EmojiPalette
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.shared.avalon.chat.models.MessageRole
import com.ensodai.shared.avalon.chat.models.Rarity
import com.ensodai.shared.avalon.chat.models.StickerGiftPayload
import com.ensodai.shared.avalon.chat.services.MessageContentCodec
import org.koin.core.annotation.Single

@Single
class OpenAiRequestBuilder(
    private val geminiRequestFactory: GeminiRequestFactory
) {

    fun buildRequest(
        dto: AICharacterConfigLLMDto,
        history: List<DomainMessage>,
        modelOverride: String?,
        messageRarity: Rarity,
        totalMessageCount: Long,
        relationshipLevel: Int,
        eventScenario: String?,
        stream: Boolean = false
    ): OpenAiChatRequest {
        val model = modelOverride?.takeIf { it.isNotBlank() } ?: dto.llmConfig.modelAPIID
        val isDirectorMode = !eventScenario.isNullOrBlank()

        val messages = mutableListOf<OpenAiChatMessage>()

        if (isDirectorMode && eventScenario != null) {
            val normalInstruction = geminiRequestFactory.buildNormalSystemInstructionText(
                dto = dto,
                rarity = messageRarity,
                messageCount = totalMessageCount,
                relationshipLevel = relationshipLevel
            )
            val reactions = EmojiPalette.getAvailableReactions().joinToString(", ")
            val directorSystemPrompt = """
                You are the "Story Event Director", a technical orchestration agent.
                Your sole task is to generate the next response in a roleplay chat on behalf of the character described in <character_profile>, introducing the narrative event/scenario described in <event_scenario_to_initiate>.

                Rules:
                1. Write strictly in the character's voice, matching their tone, speech pattern, and formatting.
                2. The user is unaware of the event. Do NOT say the user suggested this. Actively propose or start it yourself.
                3. Weave this event naturally into the context of the recent chat messages.
                4. Output ONLY the character's message. Do NOT output any system tags, thoughts, or metadata.
                5. At the very END of your response, strictly on a new line, you MUST output a reaction tag if appropriate using the syntax: <reaction>EMOJI</reaction>.
                   Allowed reactions: [$reactions]
            """.trimIndent()

            messages.add(OpenAiChatMessage(role = "system", content = directorSystemPrompt))

            val serializedHistory = history.serializeForDirector()
            val userDirectorPrompt = """
                <character_profile>
                $normalInstruction
                </character_profile>

                <conversation_history>
                $serializedHistory
                </conversation_history>

                <event_scenario_to_initiate>
                $eventScenario
                </event_scenario_to_initiate>
            """.trimIndent()

            messages.add(OpenAiChatMessage(role = "user", content = userDirectorPrompt))
        } else {
            val systemInstructionText = geminiRequestFactory.buildNormalSystemInstructionText(
                dto = dto,
                rarity = messageRarity,
                messageCount = totalMessageCount,
                relationshipLevel = relationshipLevel
            )
            messages.add(OpenAiChatMessage(role = "system", content = systemInstructionText))

            // Добавляем историю сообщений
            history.forEach { msg ->
                val role = when (msg.role) {
                    MessageRole.USER -> "user"
                    MessageRole.MODEL -> "assistant"
                    MessageRole.SYSTEM -> "system"
                }

                val text = buildMessageText(msg)
                if (text.isNotBlank()) {
                    messages.add(OpenAiChatMessage(role = role, content = text))
                }
            }
        }

        return OpenAiChatRequest(
            model = model,
            messages = messages,
            temperature = dto.temperature.coerceIn(0.1f, 2.0f),
            topP = dto.topP.takeIf { it > 0f },
            maxTokens = dto.globalSettings.maxOutputTokens.takeIf { it > 0 },
            stream = stream,
            streamOptions = if (stream) OpenAiStreamOptions(includeUsage = true) else null
        )
    }

    private fun buildMessageText(domainMessage: DomainMessage): String {
        return buildString {
            domainMessage.contents.forEach { part ->
                when (part.type) {
                    AvalonMessageContentType.TEXT -> {
                        append(part.content.trim())
                        append(" ")
                    }
                    AvalonMessageContentType.GIFT -> {
                        try {
                            val gift = MessageContentCodec.decode(
                                type = AvalonMessageContentType.GIFT,
                                contentString = part.content,
                            ) as? StickerGiftPayload
                            append("*user sent gift: ${gift?.giftName}* ")
                        } catch (e: Exception) {
                            append("*user sent gift* ")
                        }
                    }
                    AvalonMessageContentType.IMAGE -> {
                        append("*sent photo* ")
                    }
                    else -> {}
                }
            }

            if (domainMessage.role == MessageRole.USER) {
                val instruction = domainMessage.contents
                    .firstOrNull { it.type == AvalonMessageContentType.MESSAGE_SYSTEM_INSTRUCTION }
                    ?.content?.trim()

                if (!instruction.isNullOrBlank()) {
                    append("\n<message_instruction>\n")
                    append(instruction)
                    append("\n</message_instruction>")
                }
            }

            if (!domainMessage.userReaction.isNullOrBlank() || !domainMessage.characterReaction.isNullOrBlank()) {
                append("\n<reaction>")
                if (!domainMessage.userReaction.isNullOrBlank()) {
                    append("\n<user>${domainMessage.userReaction}</user>")
                }
                if (!domainMessage.characterReaction.isNullOrBlank()) {
                    append("\n<character>${domainMessage.characterReaction}</character>")
                }
                append("\n</reaction>")
            }
        }.trim()
    }

    private fun List<DomainMessage>.serializeForDirector(): String {
        return this.joinToString("\n") { domainMessage ->
            val role = when (domainMessage.role) {
                MessageRole.USER -> "User"
                MessageRole.MODEL -> "Character"
                else -> "Character"
            }
            val text = domainMessage.contents.joinToString(" ") { part ->
                when (part.type) {
                    AvalonMessageContentType.TEXT -> part.content.trim()
                    AvalonMessageContentType.GIFT -> {
                        val giftName = try {
                            val gift = MessageContentCodec.decode(
                                type = AvalonMessageContentType.GIFT,
                                contentString = part.content,
                            ) as? StickerGiftPayload
                            gift?.giftName
                        } catch (e: Exception) {
                            null
                        }
                        if (giftName != null) "*sent gift: $giftName*" else "*sent gift*"
                    }
                    AvalonMessageContentType.IMAGE -> "*sent photo*"
                    else -> ""
                }
            }.trim()

            "$role: $text"
        }
    }
}
