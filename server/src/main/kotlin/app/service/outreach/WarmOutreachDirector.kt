package app.service.outreach

import app.data.db.tables.LlmConfig
import app.data.models.AICharacterConfigLLMDto
import app.data.repository.ChatRepository
import app.domain.models.DomainMessage
import app.service.LlmConfigService
import app.service.LlmResult
import app.service.LlmService
import app.service.chat.CharacterLLMConfigService
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.shared.avalon.chat.models.MessageRole
import com.ensodai.shared.avalon.chat.models.Rarity
import com.ensodai.shared.avalon.chat.models.StickerGiftPayload
import com.ensodai.shared.avalon.chat.services.MessageContentCodec
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class WarmOutreachDirector(
    private val chatRepository: ChatRepository,
    private val llmService: LlmService,
    private val llmConfigService: LlmConfigService,
    private val characterLLMConfigService: CharacterLLMConfigService
) {
    private val log = KotlinLogging.logger {}

    /**
     * Генерирует проактивное сообщение от лица персонажа с использованием Gemini API
     * в режиме "Director Mode", учитывая историю диалога, время суток и кастомные пожелания.
     */
    suspend fun generateWarmOutreach(
        userId: Uuid,
        characterId: Uuid,
        chatVariantId: Uuid,
        relationshipLevel: Int,
        slotName: String, // "MORNING", "AFTERNOON", "EVENING"
        userPrompt: String?,
        modelCode: String
    ): LlmResult {
        log.info { "WarmOutreachDirector: Запуск генерации сообщения для пользователя $userId от персонажа $characterId в слот $slotName..." }

        // 1. Получаем LlmConfig
        val llmConfig = llmConfigService.getConfig(modelCode)

        // 2. Получаем AICharacterConfigLLMDto
        val characterLLMConfig = characterLLMConfigService.get(chatVariantId, llmConfig)
            ?: throw IllegalStateException("Не удалось получить конфигурацию персонажа для variantId: $chatVariantId")

        // 3. Загружаем историю последних 15 сообщений для контекста
        val historyResponse = chatRepository.getHistoryForVariant(chatVariantId, 15)
        val historyMessages = historyResponse.messages

        // 4. Формируем специальную проактивную директиву для технического Режиссера
        val eventScenario = buildOutreachScenarioPrompt(slotName, userPrompt)

        // 5. Вызываем LlmService в режиме Режиссера (передаем eventScenario)
        return llmService.generateResponse(
            aICharacterConfigLLMDto = characterLLMConfig,
            history = historyMessages,
            messageRarity = Rarity.COMMON,
            totalMessageCount = historyResponse.totalCount,
            relationshipLevel = relationshipLevel,
            eventScenario = eventScenario
        )
    }

    /**
     * Сборка промпта-директивы для Gemini Outreach Director.
     */
    private fun buildOutreachScenarioPrompt(slotName: String, userPrompt: String?): String {
        return """
            [Outreach Time Slot: $slotName]
            User's local time: [Period of the day: $slotName].

            ${if (!userPrompt.isNullOrBlank()) "Special user request/theme for this period: \"$userPrompt\"" else "No special topic request. Start a casual conversation based on the character's personality and recent messages."}

            Generation Instructions:
            1. Write a warm proactive message appropriate for the current time of the day ($slotName).
            2. Carefully analyze the recent conversation history in <conversation_history>.
               - If the conversation ended recently and a significant topic was discussed (e.g., job, plans, mood, fatigue, exams, medical visits), you MUST start by asking an affectionate follow-up question or making a relevant comment about it (e.g., "How did your presentation go?", "You mentioned you were exhausted last night, did you manage to get some sleep?").
               - If the conversation has cooled down or is naturally finished, initiate a new topic that fits the time of the day ($slotName) and naturally weaves in the user's special request if one is provided.
            3. The message must be short (1-3 sentences), lively, emotional, and written strictly in the first-person perspective of the character.
            4. Output ONLY the raw character's message. Do NOT include any system prefixes, quotes, or thoughts.
        """.trimIndent()
    }
}
