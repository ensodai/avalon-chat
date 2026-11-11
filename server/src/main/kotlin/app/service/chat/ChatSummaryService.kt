package app.service.chat

import com.ensodai.shared.avalon.chat.models.*
import com.ensodai.shared.avalon.gifts.models.*
import com.ensodai.shared.avalon.chat.services.MessageContentCodec
import com.ensodai.shared.avalon.gifts.models.PremiumStatus
import app.data.repository.GlobalSettingsRepository
import app.data.db.tables.chat.*
import app.data.db.tables.chatSessions.*
import app.data.network.models.openai.OpenAiChatMessage
import app.data.network.models.openai.OpenAiChatRequest
import app.domain.chat.billing.AiExecutionStrategy
import app.domain.chat.billing.AiStrategyResolver
import app.service.LlmResult
import app.service.VertexAuthData
import app.service.withConfiguredProxy
import app.service.openai.OpenAiCompatibleClient
import app.utils.dbQuery
import app.utils.logger.LlmLogger
import com.google.genai.Client
import com.google.genai.types.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.coroutines.cancellation.CancellationException
import kotlin.uuid.Uuid
import com.ensodai.shared.avalon.user.models.AiModelCategory
import com.ensodai.shared.avalon.user.models.AiProviderType
import app.service.ai.AiModelCatalogService
import org.koin.core.annotation.Single

@Single
class ChatSummaryService(
    private val vertexAuthData: VertexAuthData,
    private val globalSettingsRepository: GlobalSettingsRepository,
    private val aiStrategyResolver: AiStrategyResolver,
    private val openAiCompatibleClient: OpenAiCompatibleClient,
    private val aiModelCatalogService: AiModelCatalogService
) {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // КОНСТАНТЫ
    private val KEEP_FRESH_COUNT = 40   // Оставляем 40 последних
    private val TRIGGER_THRESHOLD = 50  // Запускаем, если > 50

    // --- Vertex Init ---
    @Volatile
    private var cachedClient: Client? = null
    private val clientMutex = Mutex()

    /**
     * Получает существующий клиент или создает новый (thread-safe).
     */
    private suspend fun getVertexClient(): Client {
        // 1. Если уже есть - возвращаем быстро
        cachedClient?.let { return it }

        // 2. Если нет - лочим поток и создаем
        return clientMutex.withLock {
            // Double-check locking (вдруг кто-то успел создать пока мы ждали лок)
            cachedClient?.let { return it }

            val location = globalSettingsRepository.getVertexLocation()

            val newClient = Client.builder()
                .vertexAI(true)
                .project(vertexAuthData.projectId)
                .location(location)
                .credentials(vertexAuthData.credentials)
                .withConfiguredProxy()
                .build()

            cachedClient = newClient
            newClient
        }
    }

    /**
     * Асинхронный запуск проверки (fire-and-forget из MessageConsumer)
     */
    fun launchCheckAndSummarize(
        chatVariantId: Uuid,
        userPremiumStatus: PremiumStatus
    ) {
        serviceScope.launch {
            try {
                checkAndSummarize(chatVariantId, userPremiumStatus)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                LlmLogger.error("SummaryService", "Background summarization failed for variant $chatVariantId: ${e.message}")
            }
        }
    }

    /**
     * Синхронный метод проверки порога и запуска
     */
    suspend fun checkAndSummarize(
        chatVariantId: Uuid,
        userPremiumStatus: PremiumStatus
    ) {
        // 1. Считаем сколько неархивированных слотов
        val unarchivedCount = dbQuery {
            MessageSlots
                .select(MessageSlots.id)
                .where {
                    (MessageSlots.chatVariantId eq chatVariantId) and
                            (MessageSlots.isArchived eq false)
                }
                .count()
        }

        // Если не превысили порог - выходим
        if (unarchivedCount <= TRIGGER_THRESHOLD) {
            return
        }

        // 2. Вычисляем сколько слотов нужно заархивировать
        val countToArchive = (unarchivedCount - KEEP_FRESH_COUNT).toInt()
        if (countToArchive <= 0) return

        // 3. Достаем самые старые слоты для сжатия
        val messagesToSummarize = fetchMessagesToSummarize(chatVariantId, countToArchive)

        if (messagesToSummarize.isEmpty()) {
            return
        }

        // 4. Запускаем обработку в зависимости от тарифа
        summarizeNow(chatVariantId, messagesToSummarize, userPremiumStatus)
    }

    /**
     * Достает текстовое содержимое старых сообщений через активные варианты
     */
    private suspend fun fetchMessagesToSummarize(
        chatVariantId: Uuid,
        limit: Int
    ): List<SummarizeMessageDto> {
        return dbQuery {
            val slotIdsToArchive = MessageSlots
                .select(MessageSlots.id)
                .where {
                    (MessageSlots.chatVariantId eq chatVariantId) and
                            (MessageSlots.isArchived eq false) and
                            (MessageSlots.deletedAt.isNull())
                }
                .orderBy(MessageSlots.createdAt, SortOrder.ASC)
                .limit(limit)
                .map { it[MessageSlots.id].value }

            if (slotIdsToArchive.isEmpty()) return@dbQuery emptyList<SummarizeMessageDto>()

            val joinQuery = MessageSlots
                .join(
                    otherTable = MessageContainers,
                    joinType = JoinType.INNER,
                    onColumn = MessageSlots.activeMessageId,
                    otherColumn = MessageContainers.id
                )
                .join(
                    otherTable = MessageContents,
                    joinType = JoinType.INNER,
                    onColumn = MessageContainers.id,
                    otherColumn = MessageContents.containerId
                )

            val rows = joinQuery
                .select(
                    MessageSlots.id,
                    MessageContainers.role,
                    MessageContents.type,
                    MessageContents.content
                )
                .where { MessageSlots.id inList slotIdsToArchive }
                .orderBy(MessageSlots.createdAt, SortOrder.ASC)
                .toList()

            rows.map {
                SummarizeMessageDto(
                    slotId = it[MessageSlots.id].value,
                    role = it[MessageContainers.role],
                    type = it[MessageContents.type],
                    content = it[MessageContents.content]
                )
            }
        }
    }

    /**
     * Маршрутизация: Free -> только архив; Premium/Elite/Custom -> LLM Саммари
     */
    private suspend fun summarizeNow(
        chatVariantId: Uuid,
        messagesToSummarize: List<SummarizeMessageDto>,
        userPremiumStatus: PremiumStatus
    ) {
        val userId = dbQuery {
            ChatVariants
                .join(ChatSessions, JoinType.INNER, ChatVariants.chatSessionId, ChatSessions.id)
                .select(ChatSessions.userId)
                .where { ChatVariants.id eq chatVariantId }
                .singleOrNull()
                ?.get(ChatSessions.userId)?.value
        }

        val strategy = userId?.let { aiStrategyResolver.resolveStrategy(it) }

        val isEligibleForSummary = when (strategy) {
            is AiExecutionStrategy.ServerVertex -> userPremiumStatus in listOf(
                PremiumStatus.PREMIUM,
                PremiumStatus.CRYSTAL,
                PremiumStatus.ELITE,
                PremiumStatus.SPECIAL
            )
            is AiExecutionStrategy.CustomProvider -> strategy.apiKey.isNotBlank()
            null -> false
        }

        if (isEligibleForSummary && strategy != null) {
            processWithLlmSummary(chatVariantId, messagesToSummarize, strategy)
        } else {
            archiveMessagesOnly(messagesToSummarize)
        }
    }

    /**
     * Логика для Premium/Elite или Custom AI: Генерируем саммари и обновляем базу
     */
    private suspend fun processWithLlmSummary(
        chatVariantId: Uuid,
        messagesToSummarize: List<SummarizeMessageDto>,
        strategy: AiExecutionStrategy
    ) {
        // 2. Получаем предыдущее саммари
        val currentSummaryText = dbQuery {
            ChatSummaries
                .select(ChatSummaries.content)
                .where { ChatSummaries.chatVariantId eq chatVariantId }
                .orderBy(ChatSummaries.createdAt, SortOrder.DESC)
                .limit(1)
                .singleOrNull()
                ?.get(ChatSummaries.content)
        }

        LlmLogger.inf("SummaryService", "Summarizing ${messagesToSummarize.size} msgs for variant $chatVariantId...")

        try {
            // 3. Вызываем Vertex AI или OpenAI/OpenRouter или Google AI Studio
            val newSummaryText = when (strategy) {
                is AiExecutionStrategy.CustomProvider -> {
                    if (strategy.apiKey.isNotBlank()) {
                        if (strategy.provider == com.ensodai.shared.avalon.user.models.AiProviderType.GOOGLE) {
                            val googleClient = Client.builder()
                                .apiKey(strategy.apiKey)
                                .withConfiguredProxy()
                                .build()
                            try {
                                executeSummarizationRequest(
                                    currentSummary = currentSummaryText,
                                    newMessages = messagesToSummarize,
                                    customClient = googleClient,
                                    modelOverride = strategy.summaryModel
                                )
                            } finally {
                                try { googleClient.close() } catch (_: Exception) {}
                            }
                        } else {
                            executeOpenAiSummarizationRequest(
                                provider = strategy.provider,
                                baseUrl = strategy.baseUrl,
                                apiKey = strategy.apiKey,
                                model = strategy.summaryModel,
                                currentSummary = currentSummaryText,
                                newMessages = messagesToSummarize
                            )
                        }
                    } else null
                }
                is AiExecutionStrategy.ServerVertex -> {
                    executeSummarizationRequest(
                        currentSummary = currentSummaryText,
                        newMessages = messagesToSummarize
                    )
                }
            }

            // Если вернулась пустота (ошибка), выходим, НЕ архивируя (попробуем в след. раз)
            if (newSummaryText.isNullOrBlank()) {
                LlmLogger.error("SummaryService", "Empty summary returned. Skipping update.")
                return
            }

            // 4. Сохраняем результат транзакционно
            dbQuery {
                // А. Пишем новое саммари
                ChatSummaries.insert {
                    it[this.chatVariantId] = chatVariantId
                    it[this.content] = newSummaryText
                    it[this.lastIncludedSlotId] = messagesToSummarize.last().slotId
                }

                // Б. Архивируем обработанные слоты
                val slotIds = messagesToSummarize.map { it.slotId }
                MessageSlots.update({ MessageSlots.id inList slotIds }) {
                    it[isArchived] = true
                }
            }
            LlmLogger.response(
                "SummaryService",
                "Summary updated (LLM). Archived ${messagesToSummarize.size} messages."
            )

        } catch (e: Exception) {
            LlmLogger.error("SummaryService", "Error during summary call: ${e.message}")
            throw e
        }
    }

    /**
     * Логика для Free/Standard: Просто помечаем старые как архивные, чтобы не тормозило
     */
    private suspend fun archiveMessagesOnly(
        messagesToSummarize: List<SummarizeMessageDto>
    ) {
        LlmLogger.inf(
            "SummaryService",
            "Quota limit reached. Archiving ${messagesToSummarize.size} msgs without LLM summary."
        )

        try {
            dbQuery {
                val slotIds = messagesToSummarize.map { it.slotId }
                MessageSlots.update({ MessageSlots.id inList slotIds }) {
                    it[isArchived] = true
                }
            }
        } catch (e: Exception) {
            LlmLogger.error("SummaryService", "Error during archiving: ${e.message}")
        }
    }

    private suspend fun executeSummarizationRequest(
        currentSummary: String?,
        newMessages: List<SummarizeMessageDto>,
        customClient: Client? = null,
        modelOverride: String? = null
    ): String {
        val client = customClient ?: getVertexClient()
        val effectiveModel = modelOverride
            ?.trim()
            ?.removePrefix("google/")
            ?.removePrefix("models/")
            ?.takeIf { it.isNotBlank() }
            ?: aiModelCatalogService.getRecommendedModel(AiProviderType.GOOGLE, AiModelCategory.SUMMARY)

        val dialogText = buildDialogText(newMessages)

        val userPromptText = if (currentSummary.isNullOrBlank()) {
            """
            Analyze the following dialogue between a User and an AI Character.
            Write a concise summary in 3rd person.
            Focus on facts, names, current events, and the user's preferences.
            Ignore standard greetings like "Hello" or "How are you" unless they have specific meaning.
            
            DIALOGUE:
            $dialogText
            """.trimIndent()
        } else {
            """
            You are maintaining a long-term memory log for a roleplay chat AI.
            
            GOAL: Update the summary to include the new lines.
            
            RULES FOR COMPRESSION:
            1. **Current Topic:** Keep full details for the topic currently being discussed (e.g., Skiing).
            2. **Old Topics:** Drastically condense finished topics into 1-2 sentences. Keep only key facts about the User (e.g., "User is a developer", "User likes speed").
            3. **Facts over Flow:** Prioritize preserving user's name, job, preferences, and specific facts over the conversation flow.
            4. **Perspective:** Write in 3rd person.
            
            OLD SUMMARY:
            $currentSummary
            
            NEW LINES TO MERGE:
            $dialogText
            
            Output ONLY the updated summary.
            """.trimIndent()
        }

        val safetySettings = listOf(
            SafetySetting.builder()
                .category(HarmCategory.Known.HARM_CATEGORY_HARASSMENT)
                .threshold(HarmBlockThreshold.Known.OFF)
                .build(),
            SafetySetting.builder()
                .category(HarmCategory.Known.HARM_CATEGORY_HATE_SPEECH)
                .threshold(HarmBlockThreshold.Known.OFF)
                .build(),
            SafetySetting.builder()
                .category(HarmCategory.Known.HARM_CATEGORY_SEXUALLY_EXPLICIT)
                .threshold(HarmBlockThreshold.Known.OFF)
                .build(),
            SafetySetting.builder()
                .category(HarmCategory.Known.HARM_CATEGORY_DANGEROUS_CONTENT)
                .threshold(HarmBlockThreshold.Known.OFF)
                .build()
        )

        val config = GenerateContentConfig.builder()
            .temperature(0.3f)
            .maxOutputTokens(1000)
            .safetySettings(safetySettings)
            .systemInstruction(
                Content.builder()
                    .role("system")
                    .parts(
                        listOf(
                            Part.fromText(
                                "You are an expert memory assistant. " +
                                        "Your goal is to maintain a consistent long-term memory summary for a roleplay chat."
                            )
                        )
                    )
                    .build()
            )
            .build()

        val content = Content.builder()
            .role("user")
            .parts(listOf(Part.fromText(userPromptText)))
            .build()

        val response = runInterruptible(Dispatchers.IO) {
            client.models.generateContent(
                effectiveModel,
                content,
                config
            )
        }

        return extractTextSafe(response)
    }

    private suspend fun executeOpenAiSummarizationRequest(
        provider: AiProviderType,
        baseUrl: String?,
        apiKey: String,
        model: String?,
        currentSummary: String?,
        newMessages: List<SummarizeMessageDto>
    ): String {
        val dialogText = buildDialogText(newMessages)
        val selectedModel = model?.takeIf { it.isNotBlank() }
            ?: aiModelCatalogService.getRecommendedModel(provider, AiModelCategory.SUMMARY)

        if (selectedModel.isBlank()) {
            LlmLogger.error("SummaryService", "No summary model found for provider $provider. Skipping LLM summary.")
            return ""
        }
        val resolvedBaseUrl = if (!baseUrl.isNullOrBlank()) baseUrl else "https://openrouter.ai/api/v1"

        val promptText = if (currentSummary.isNullOrBlank()) {
            """
            Analyze the following dialogue between a User and an AI Character.
            Write a concise summary in 3rd person.
            Focus on facts, names, current events, and the user's preferences.
            Ignore standard greetings like "Hello" or "How are you" unless they have specific meaning.
            
            DIALOGUE:
            $dialogText
            """.trimIndent()
        } else {
            """
            You are maintaining a long-term memory log for a roleplay chat AI.
            
            GOAL: Update the summary to include the new lines.
            
            RULES FOR COMPRESSION:
            1. **Current Topic:** Keep full details for the topic currently being discussed (e.g., Skiing).
            2. **Old Topics:** Drastically condense finished topics into 1-2 sentences. Keep only key facts about the User (e.g., "User is a developer", "User likes speed").
            3. **Facts over Flow:** Prioritize preserving user's name, job, preferences, and specific facts over the conversation flow.
            4. **Perspective:** Write in 3rd person.
            
            OLD SUMMARY:
            $currentSummary
            
            NEW LINES TO MERGE:
            $dialogText
            
            Output ONLY the updated summary.
            """.trimIndent()
        }

        val request = OpenAiChatRequest(
            model = selectedModel,
            messages = listOf(
                OpenAiChatMessage(
                    role = "user",
                    content = promptText
                )
            ),
            temperature = 0.3f,
            maxTokens = 800,
            stream = false
        )

        val result = openAiCompatibleClient.generateChatCompletion(
            baseUrl = resolvedBaseUrl,
            apiKey = apiKey,
            request = request,
            reqId = "summary-${System.currentTimeMillis()}"
        )

        return when (result) {
            is LlmResult.Success -> result.text
            else -> ""
        }
    }

    private fun buildDialogText(newMessages: List<SummarizeMessageDto>): String {
        val dialogText = StringBuilder()
        newMessages.forEach { msg ->
            val roleStr = if (msg.role.name == "USER") "User" else "Character"
            val cleanContent = when (msg.type) {
                AvalonMessageContentType.TEXT -> msg.content.trim()
                AvalonMessageContentType.GIFT -> {
                    try {
                        val gift = MessageContentCodec.decode(
                            type = AvalonMessageContentType.GIFT,
                            contentString = msg.content,
                        ) as? StickerGiftPayload
                        if (gift != null) "*sent gift: ${gift.giftName}*" else "*sent gift*"
                    } catch (e: Exception) {
                        "*sent gift*"
                    }
                }
                AvalonMessageContentType.IMAGE -> "*sent photo*"
                else -> msg.content.trim()
            }
            if (cleanContent.isNotBlank()) {
                dialogText.append("$roleStr: $cleanContent\n")
            }
        }
        return dialogText.toString()
    }

    private fun extractTextSafe(response: GenerateContentResponse): String {
        val candidates = response.candidates().orElse(null)
        if (candidates.isNullOrEmpty()) return ""

        val finishReasonObj = response.finishReason()
        val reasonEnum = finishReasonObj.knownEnum()

        val isContentValid = when (reasonEnum) {
            FinishReason.Known.STOP,
            FinishReason.Known.FINISH_REASON_UNSPECIFIED,
            FinishReason.Known.MAX_TOKENS -> true

            else -> {
                LlmLogger.error("SummaryService", "Blocked/Failed: $reasonEnum")
                false
            }
        }

        if (!isContentValid) return ""

        val firstCandidate = candidates[0]
        val sb = StringBuilder()

        firstCandidate.content().orElse(null)?.parts()?.orElse(null)?.forEach { part ->
            part.text().orElse(null)?.let { if (it.isNotEmpty()) sb.append(it) }
        }

        return sb.toString().trim()
    }

    private data class SummarizeMessageDto(
        val slotId: Uuid,
        val role: MessageRole,
        val type: AvalonMessageContentType,
        val content: String
    )
}