package app.service
import com.ensodai.shared.avalon.gifts.models.*

import app.data.db.tables.users.AppUsers
import com.ensodai.shared.avalon.gifts.models.PremiumStatus
import app.data.repository.GlobalSettingsRepository
import app.service.*
import com.ensodai.shared.avalon.chat.models.Rarity
import app.data.models.AICharacterConfigLLMDto
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.shared.avalon.chat.models.MessageRole
import app.domain.models.DomainApiKey
import app.domain.models.DomainMessage
import app.service.chat.EmojiExtractor
import app.service.chat.EmojiPalette
import app.service.chat.ReactionParser
import com.ensodai.shared.avalon.chat.services.MessageContentCodec
import com.ensodai.shared.avalon.chat.models.StickerGiftPayload
import app.utils.EnvProvider
import app.utils.dbQuery
import app.utils.logger.LlmLogger
import app.utils.orNullOpt
import com.google.genai.Client
import com.google.genai.types.*
import com.sun.org.slf4j.internal.LoggerFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.select

import app.data.repository.UserAiSettingsRepository
import app.service.openai.OpenAiCompatibleClient
import app.service.openai.OpenAiRequestBuilder
import kotlinx.coroutines.flow.emitAll
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.cancellation.CancellationException
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single

sealed class LlmResult {
    data class TokenStats(
        val totalTokens: Long,
        val promptTokens: Long,
        val outputTokens: Long,
        val cachedTokens: Long = 0L,
        val thoughtsTokens: Long = 0L,
        val openRouterCostUsd: java.math.BigDecimal? = null
    )
    
    data class Success(
        val text: String, 
        val tokens: Long, 
        val reaction: String? = null,
        val tokenStats: TokenStats? = null
    ) : LlmResult()
    
    data class Blocked(val reason: String) : LlmResult()
    data class Error(val exception: Throwable) : LlmResult()
}

data class LlmStreamChunk(
    val textDelta: String,
    val isFinished: Boolean,
    val reaction: String? = null,
    val tokenStats: LlmResult.TokenStats? = null
)

@Single
class LlmService(
    private val apiKeyProvider: ApiKeyProvider,
    private val requestFactory: GeminiRequestFactory,
    private val exceptionHandler: GeminiExceptionHandler,
    private val globalSettingsRepository: GlobalSettingsRepository,
    private val vertexAuthData: VertexAuthData,
    private val aiStrategyResolver: app.domain.chat.billing.AiStrategyResolver,
    private val openAiCompatibleClient: OpenAiCompatibleClient,
    private val openAiRequestBuilder: OpenAiRequestBuilder
) {

    private val cachedVertexClients = ConcurrentHashMap<String, Client>()
    private val vertexMutex = Mutex()

    /**
     * Получает или создает Vertex Client (Thread-safe Singleton).
     */
    private suspend fun getVertexClient(location: String): Client {
        cachedVertexClients[location]?.let { return it }

        return vertexMutex.withLock {
            cachedVertexClients[location]?.let { return it }

            // 1. Базовая настройка клиента
            val clientBuilder = Client.builder()
                .vertexAI(true)
                .project(vertexAuthData.projectId)
                .credentials(vertexAuthData.credentials)
                .location(location) // Если это Gemini 3, сюда должно приходить "us-central1"
                .withConfiguredProxy()

            val newClient = clientBuilder.build()
            cachedVertexClients[location] = newClient
            newClient
        }
    }

    /**
     * Основной метод генерации ответа.
     * Управляет жизненным циклом запроса: подбором ключей, повторными попытками (retries)
     * и ротацией ключей при ошибках квот.
     */
    suspend fun generateResponse(
        aICharacterConfigLLMDto: AICharacterConfigLLMDto,
        history: List<DomainMessage>,
        messageRarity: Rarity,
        totalMessageCount: Long,
        relationshipLevel: Int = 1,
        eventScenario: String? = null
    ): LlmResult {
        val reqId = "usr-${aICharacterConfigLLMDto.userId}"
        val strategy = aiStrategyResolver.resolveStrategy(aICharacterConfigLLMDto.userId)

        // 1. Не-Google кастомный провайдер (OpenRouter, OpenAI, Groq, DeepSeek, Custom URL)
        if (strategy is app.domain.chat.billing.AiExecutionStrategy.CustomProvider && strategy.provider != com.ensodai.shared.avalon.user.models.AiProviderType.GOOGLE) {
            if (strategy.apiKey.isBlank()) {
                return LlmResult.Blocked("API_KEY_REQUIRED")
            }

            val lastUserMsg = history.lastOrNull { it.role == com.ensodai.shared.avalon.chat.models.MessageRole.USER }
                ?.contents?.joinToString(" ") { it.content } ?: "[No text]"
            LlmLogger.prompt(reqId, lastUserMsg)
            val request = openAiRequestBuilder.buildRequest(
                dto = aICharacterConfigLLMDto,
                history = history,
                modelOverride = strategy.textModel,
                messageRarity = messageRarity,
                totalMessageCount = totalMessageCount,
                relationshipLevel = relationshipLevel,
                eventScenario = eventScenario,
                stream = false
            )

            return openAiCompatibleClient.generateChatCompletion(
                baseUrl = strategy.baseUrl ?: "https://openrouter.ai/api/v1",
                apiKey = strategy.apiKey,
                request = request,
                reqId = reqId
            )
        }

        // 2. Google кастомный провайдер с пустым ключом
        if (strategy is app.domain.chat.billing.AiExecutionStrategy.CustomProvider && strategy.provider == com.ensodai.shared.avalon.user.models.AiProviderType.GOOGLE && strategy.apiKey.isBlank()) {
            return LlmResult.Blocked("API_KEY_REQUIRED")
        }

        val isUserGoogleByok = strategy is app.domain.chat.billing.AiExecutionStrategy.CustomProvider && strategy.provider == com.ensodai.shared.avalon.user.models.AiProviderType.GOOGLE
        val useVertex = !isUserGoogleByok
        val userStatus = getUserPremiumStatus(aICharacterConfigLLMDto.userId)
        val vertexLocation = globalSettingsRepository.getVertexLocation()

        val isDirectorMode = !eventScenario.isNullOrBlank()

        val historyContent = if (isDirectorMode && eventScenario != null) {
            val normalInstruction = requestFactory.buildNormalSystemInstructionText(
                dto = aICharacterConfigLLMDto,
                rarity = messageRarity,
                messageCount = totalMessageCount,
                relationshipLevel = relationshipLevel
            )
            val serializedHistory = history.serializeForDirector()
            val promptText = """
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

            listOf(
                Content.builder()
                    .role("user")
                    .parts(listOf(Part.fromText(promptText)))
                    .build()
            )
        } else {
            history.toGoogleContent()
        }

        LlmLogger.inf(
            reqId,
            "Start gen. Mode: ${if (isUserGoogleByok) "🔑 GOOGLE AI STUDIO (BYOK)" else if (useVertex) "⚡ VERTEX AI ($vertexLocation)" else "🔑 LEGACY API KEYS"}"
        )

        var serverRetryAttempts = 0
        var keyRotationAttempts = 0
        val maxServerRetries = 3
        val maxKeyRotations = 15

        while (serverRetryAttempts < maxServerRetries && keyRotationAttempts < maxKeyRotations) {

            var currentApiKey: DomainApiKey? = null
            var client: Client? = null

            // ВАЖНО: Флаг, который решает, убивать ли клиент после запроса
            var shouldCloseClient = false

            try {
                // 1. Настройка Клиента и Конфига
                val configContainer = if (isUserGoogleByok) {
                    val userKey = (strategy as app.domain.chat.billing.AiExecutionStrategy.CustomProvider).apiKey
                    client = createLegacyGeminiClient(userKey)
                    shouldCloseClient = true

                    requestFactory.createConfig(
                        aICharacterConfigLLMDto = aICharacterConfigLLMDto,
                        apiKey = DomainApiKey(Uuid.random(), userKey),
                        messageRarity = messageRarity,
                        messageCount = totalMessageCount,
                        relationshipLevel = relationshipLevel,
                        isDirectorMode = isDirectorMode,
                        modelOverride = strategy.textModel
                    )
                } else if (useVertex) {
                    // --- VERTEX (Синглтон) ---
                    client = getVertexClient(vertexLocation) // Берем из кеша (getVertexClient сам разберется с mutex)
                    shouldCloseClient = false  // НЕ ЗАКРЫВАТЬ! Он нужен другим юзерам.

                    // Ключ не нужен, суем заглушку
                    requestFactory.createConfig(
                        aICharacterConfigLLMDto = aICharacterConfigLLMDto,
                        apiKey = DomainApiKey(Uuid.random(), "vertex"),
                        messageRarity = messageRarity,
                        messageCount = totalMessageCount,
                        relationshipLevel = relationshipLevel,
                        isDirectorMode = isDirectorMode
                    )
                } else {
                    // --- LEGACY (Одноразовый) ---
                    currentApiKey = apiKeyProvider.getAvailableKey(userStatus)
                        ?: return LlmResult.Error(Exception("No available API keys."))

                    client = createLegacyGeminiClient(currentApiKey.key)
                    shouldCloseClient = true   // ЗАКРЫВАТЬ! Чтобы не текли ресурсы.

                    requestFactory.createConfig(
                        aICharacterConfigLLMDto = aICharacterConfigLLMDto,
                        apiKey = currentApiKey,
                        messageRarity = messageRarity,
                        messageCount = totalMessageCount,
                        relationshipLevel = relationshipLevel,
                        isDirectorMode = isDirectorMode
                    )
                }

                // 2. Проверка на null (на всякий случай)

                // 3. Выполнение запроса
                val response = executeRequest(
                    client = client,
                    modelName = configContainer.modelAPIID,
                    history = historyContent,
                    config = configContainer.javaSdkConfig,
                    reqId = reqId
                )

                return processResponse(response, reqId)

            } catch (e: Exception) {
                if (e is CancellationException) throw e
                LlmLogger.error(reqId, "Error: ${e.message}")

                if (useVertex) {
                    val errorType = exceptionHandler.handle(e)
                    if (errorType is GeminiError.ServerError) {
                        serverRetryAttempts++
                        delay(1000L * serverRetryAttempts)
                        continue
                    }
                    return LlmResult.Error(e)
                } else {
                    // Legacy logic
                    // Тут безопасно использовать currentApiKey!!, так как мы в ветке else (Legacy), где он точно задан
                    val apiKeyId = currentApiKey?.id ?: Uuid.random()
                    val keySuffix = currentApiKey?.key?.takeLast(4) ?: "????"

                    val decision = handleException(e, reqId, apiKeyId, "...$keySuffix")
                    when (decision) {
                        ErrorAction.ROTATE_KEY -> {
                            keyRotationAttempts++; continue
                        }

                        ErrorAction.RETRY_SERVER -> {
                            serverRetryAttempts++; delay(1000L * serverRetryAttempts); continue
                        }

                        ErrorAction.FATAL -> return LlmResult.Error(e)
                    }
                }
            } finally {
                if (shouldCloseClient) {
                    client?.close()
                }
            }
        }
        return LlmResult.Error(Exception("Max attempts reached"))
    }


    private fun createLegacyGeminiClient(apiKey: String): Client {
        return Client.builder()
            .apiKey(apiKey)
            .withConfiguredProxy()
            .build()
    }

    /**
     * Потоковая генерация ответа через Flow.
     */
    fun generateResponseStream(
        aICharacterConfigLLMDto: AICharacterConfigLLMDto,
        history: List<DomainMessage>,
        messageRarity: Rarity,
        totalMessageCount: Long,
        relationshipLevel: Int = 1,
        eventScenario: String? = null
    ): kotlinx.coroutines.flow.Flow<LlmStreamChunk> = kotlinx.coroutines.flow.flow {
        val reqId = "usr-stream-${aICharacterConfigLLMDto.userId}"
        val strategy = aiStrategyResolver.resolveStrategy(aICharacterConfigLLMDto.userId)

        // 1. Не-Google кастомный провайдер (OpenRouter, OpenAI, Groq, DeepSeek, Custom URL)
        if (strategy is app.domain.chat.billing.AiExecutionStrategy.CustomProvider && strategy.provider != com.ensodai.shared.avalon.user.models.AiProviderType.GOOGLE) {
            if (strategy.apiKey.isBlank()) {
                throw IllegalStateException("API_KEY_REQUIRED")
            }

            val lastUserMsg = history.lastOrNull { it.role == com.ensodai.shared.avalon.chat.models.MessageRole.USER }
                ?.contents?.joinToString(" ") { it.content } ?: "[No text]"
            LlmLogger.prompt(reqId, lastUserMsg)
            val request = openAiRequestBuilder.buildRequest(
                dto = aICharacterConfigLLMDto,
                history = history,
                modelOverride = strategy.textModel,
                messageRarity = messageRarity,
                totalMessageCount = totalMessageCount,
                relationshipLevel = relationshipLevel,
                eventScenario = eventScenario,
                stream = true
            )

            emitAll(
                openAiCompatibleClient.generateChatCompletionStream(
                    baseUrl = strategy.baseUrl ?: "https://openrouter.ai/api/v1",
                    apiKey = strategy.apiKey,
                    request = request,
                    reqId = reqId
                )
            )
            return@flow
        }

        // 2. Google кастомный провайдер с пустым ключом
        if (strategy is app.domain.chat.billing.AiExecutionStrategy.CustomProvider && strategy.provider == com.ensodai.shared.avalon.user.models.AiProviderType.GOOGLE && strategy.apiKey.isBlank()) {
            throw IllegalStateException("API_KEY_REQUIRED")
        }

        val isUserGoogleByok = strategy is app.domain.chat.billing.AiExecutionStrategy.CustomProvider && strategy.provider == com.ensodai.shared.avalon.user.models.AiProviderType.GOOGLE
        val useVertex = !isUserGoogleByok
        val userStatus = getUserPremiumStatus(aICharacterConfigLLMDto.userId)
        val vertexLocation = globalSettingsRepository.getVertexLocation()
        val isDirectorMode = !eventScenario.isNullOrBlank()

        val historyContent = if (isDirectorMode && eventScenario != null) {
            val normalInstruction = requestFactory.buildNormalSystemInstructionText(
                dto = aICharacterConfigLLMDto,
                rarity = messageRarity,
                messageCount = totalMessageCount,
                relationshipLevel = relationshipLevel
            )
            val serializedHistory = history.serializeForDirector()
            val promptText = """
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
            listOf(
                Content.builder()
                    .role("user")
                    .parts(listOf(Part.fromText(promptText)))
                    .build()
            )
        } else {
            history.toGoogleContent()
        }

        var client: Client? = null
        var shouldCloseClient = false
        var currentApiKey: DomainApiKey? = null

        val configContainer = if (isUserGoogleByok) {
            val userKey = (strategy as app.domain.chat.billing.AiExecutionStrategy.CustomProvider).apiKey
            client = createLegacyGeminiClient(userKey)
            shouldCloseClient = true
            requestFactory.createConfig(
                aICharacterConfigLLMDto = aICharacterConfigLLMDto,
                apiKey = DomainApiKey(Uuid.random(), userKey),
                messageRarity = messageRarity,
                messageCount = totalMessageCount,
                relationshipLevel = relationshipLevel,
                isDirectorMode = isDirectorMode,
                modelOverride = strategy.textModel
            )
        } else if (useVertex) {
            client = getVertexClient(vertexLocation)
            shouldCloseClient = false
            requestFactory.createConfig(
                aICharacterConfigLLMDto = aICharacterConfigLLMDto,
                apiKey = DomainApiKey(Uuid.random(), "vertex"),
                messageRarity = messageRarity,
                messageCount = totalMessageCount,
                relationshipLevel = relationshipLevel,
                isDirectorMode = isDirectorMode
            )
        } else {
            currentApiKey = apiKeyProvider.getAvailableKey(userStatus)
                ?: throw Exception("No available API keys.")
            client = createLegacyGeminiClient(currentApiKey.key)
            shouldCloseClient = true
            requestFactory.createConfig(
                aICharacterConfigLLMDto = aICharacterConfigLLMDto,
                apiKey = currentApiKey,
                messageRarity = messageRarity,
                messageCount = totalMessageCount,
                relationshipLevel = relationshipLevel,
                isDirectorMode = isDirectorMode
            )
        }
        
        LlmLogger.processing(reqId, configContainer.modelAPIID)
        
        var totalTokens = 0L
        var promptTokens = 0L
        var outputTokens = 0L
        var cachedTokens = 0L
        var thoughtsTokens = 0L

        try {
            val stream = client.models.generateContentStream(configContainer.modelAPIID, historyContent, configContainer.javaSdkConfig)
            val reactionFilter = StreamingReactionFilter()
            
            val iterator = stream.iterator()
            while (iterator.hasNext()) {
                val response = iterator.next()
                val promptBlockReason = response.promptFeedback().orNullOpt()?.blockReason()?.orNullOpt()
                if (promptBlockReason != null && promptBlockReason.toString() != "BLOCKED_REASON_UNSPECIFIED") {
                    LlmLogger.inf(reqId, "⛔ Prompt blocked stream. Reason: $promptBlockReason")
                    throw Exception("BLOCKED_PROMPT")
                }

                val finishReason = response.candidates().orNullOpt()?.firstOrNull()?.finishReason()?.orNullOpt()?.toString()
                if (finishReason == "SAFETY" || finishReason == "BLOCKLIST") {
                    LlmLogger.inf(reqId, "⛔ Content blocked stream. Reason: $finishReason")
                    throw Exception("BLOCKED_CONTENT")
                }

                val chunkText = response.candidates().orNullOpt()?.firstOrNull()
                    ?.content()?.orNullOpt()
                    ?.parts()?.orNullOpt()?.firstOrNull()
                    ?.text()?.orNullOpt() ?: ""
                    
                val (cleanDelta, reaction) = reactionFilter.process(chunkText)
                
                val metadata = response.usageMetadata().orNullOpt()
                if (metadata != null) {
                    totalTokens = metadata.totalTokenCount()?.orNullOpt()?.toLong() ?: totalTokens
                    promptTokens = metadata.promptTokenCount()?.orNullOpt()?.toLong() ?: promptTokens
                    outputTokens = metadata.candidatesTokenCount()?.orNullOpt()?.toLong() ?: outputTokens
                    cachedTokens = metadata.cachedContentTokenCount()?.orNullOpt()?.toLong() ?: cachedTokens
                    thoughtsTokens = metadata.thoughtsTokenCount()?.orNullOpt()?.toLong() ?: thoughtsTokens
                }

                if (cleanDelta.isNotEmpty() || reaction != null) {
                    emit(LlmStreamChunk(
                        textDelta = cleanDelta,
                        isFinished = false,
                        reaction = reaction,
                        tokenStats = null
                    ))
                }
            }
            
            val finalFlush = reactionFilter.flush()
            val pendingReaction = reactionFilter.getPendingReaction()
            val tokenStats = LlmResult.TokenStats(totalTokens, promptTokens, outputTokens, cachedTokens, thoughtsTokens)
            
            emit(LlmStreamChunk(
                textDelta = finalFlush,
                isFinished = true,
                reaction = pendingReaction,
                tokenStats = tokenStats
            ))
            
            LlmLogger.response(reqId, "Stream Success. Total: $totalTokens")
            
        } catch (e: Exception) {
            LlmLogger.error(reqId, "Stream Error: ${e.message}")
            throw e
        } finally {
            if (shouldCloseClient) {
                try {
                    client?.close()
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    /**
     * Выполняет сетевой запрос к Google API в Stateless режиме.
     * Использует `models.generateContent` вместо `chats.sendMessage`, чтобы избежать
     * внутренних ошибок SDK при сохранении истории (проблема "No value present").
     */
    private suspend fun executeRequest(
        client: Client,
        modelName: String,
        history: List<Content>,
        config: GenerateContentConfig,
        reqId: String
    ): GenerateContentResponse {
        LlmLogger.processing(reqId, modelName)

        return runInterruptible(Dispatchers.IO) {
            client.models.generateContent(
                modelName,
                history,
                config
            )
        }
    }

    /**
     * Безопасно разбирает ответ от Gemini.
     * Проверяет причины блокировки (Safety Filters) и корректно извлекает текст,
     * избегая исключений `NoSuchElementException` на пустых Optional.
     */
    private fun processResponse(response: GenerateContentResponse, reqId: String): LlmResult {
        // === 1. Проверяем блокировку Промпта ===
        val promptBlockReason = response.promptFeedback().orNullOpt()
            ?.blockReason()?.orNullOpt()

        // Проверяем, что причина есть и она не "UNSPECIFIED" (пустышка)
        if (promptBlockReason != null && promptBlockReason.toString() != "BLOCKED_REASON_UNSPECIFIED") {
            LlmLogger.inf(reqId, "⛔ Prompt blocked. Reason: $promptBlockReason")
            return LlmResult.Blocked("PROMPT_BLOCKED: $promptBlockReason")
        }

        // === 2. Проверяем наличие Кандидатов ===
        // Если candidates() вернул пустой Optional или пустой список — это блокировка
        val candidates = response.candidates().orNullOpt()
        if (candidates.isNullOrEmpty()) {
            LlmLogger.inf(reqId, "⛔ No candidates returned (likely Safety Block).")
            return LlmResult.Blocked("NO_CANDIDATES_SAFETY")
        }

        // Берем первого кандидата (обычно он один)
        val firstCandidate = candidates[0]

        // === 3. Проверяем причину завершения ===
        val finishReason = firstCandidate.finishReason().orNullOpt()?.knownEnum()

        when (finishReason) {
            FinishReason.Known.SAFETY,
            FinishReason.Known.PROHIBITED_CONTENT,
            FinishReason.Known.RECITATION,
            FinishReason.Known.SPII,
            FinishReason.Known.BLOCKLIST,
            FinishReason.Known.IMAGE_SAFETY -> {
                LlmLogger.inf(reqId, "⛔ Content blocked. FinishReason: $finishReason")
                return LlmResult.Blocked(finishReason.toString())
            }

            else -> {
                LlmLogger.inf(reqId, "⛔ Content stopped. FinishReason: $finishReason")
            }
        }

        // === 4. Извлечение текста вручную ===
        // ВАЖНО: Не используем response.text(), так как он может вызвать краш.
        // Собираем текст по кусочкам.
        val sb = StringBuilder()
        val parts = firstCandidate.content().orNullOpt()?.parts()?.orNullOpt()

        parts?.forEach { part ->
            val textPart = part.text().orNullOpt()
            if (!textPart.isNullOrEmpty()) {
                sb.append(textPart)
            }
        }

        val rawResponseText = sb.toString()

        // === 5. ПАРСИНГ ТЕКСТА И РЕАКЦИИ ===
        val (finalText, reaction) = ReactionParser.parseFullText(rawResponseText)

        // === 6. Возврат результата ===
        if (finalText.isNotBlank()) {
            val metadata = response.usageMetadata().orNullOpt()
            val totalTokens = metadata?.totalTokenCount()?.orNullOpt()?.toLong() ?: 0L
            val promptTokens = metadata?.promptTokenCount()?.orNullOpt()?.toLong() ?: 0L
            val outputTokens = metadata?.candidatesTokenCount()?.orNullOpt()?.toLong() ?: 0L
            val cachedTokens = metadata?.cachedContentTokenCount()?.orNullOpt()?.toLong() ?: 0L
            val thoughtsTokens = metadata?.thoughtsTokenCount()?.orNullOpt()?.toLong() ?: 0L

            val tokenStats = LlmResult.TokenStats(
                totalTokens = totalTokens,
                promptTokens = promptTokens,
                outputTokens = outputTokens,
                cachedTokens = cachedTokens,
                thoughtsTokens = thoughtsTokens
            )

            LlmLogger.response(
                reqId, 
                "Success. Length: ${finalText.length}. Total: $totalTokens (prompt: $promptTokens, output: $outputTokens). Cached: $cachedTokens. Thoughts: $thoughtsTokens. Reaction: $reaction"
            )
            return LlmResult.Success(finalText, totalTokens, reaction, tokenStats)
        }

        LlmLogger.inf(reqId, "⚠️ Empty response text. FinishReason: $finishReason")
        return LlmResult.Blocked("EMPTY_CONTENT")
    }

    /**
     * Обрабатывает исключения и определяет дальнейшие действия.
     */
    private suspend fun handleException(
        e: Exception,
        reqId: String,
        apiKeyId: Uuid, // Замените на тип ID вашего ключа (Long/String)
        maskedKey: String
    ): ErrorAction {
        return when (val errorType = exceptionHandler.handle(e)) {
            is GeminiError.QuotaExceeded,
            is GeminiError.KeyBanned -> {
                LlmLogger.inf(reqId, "♻️ Key issue (${errorType::class.simpleName}) on key $maskedKey. Rotating...")
                apiKeyProvider.reportKeyFailure(apiKeyId, errorType)
                ErrorAction.ROTATE_KEY
            }

            is GeminiError.ServerError -> {
                LlmLogger.error(reqId, "⚠️ Server Error (Google). Scheduling retry...")
                ErrorAction.RETRY_SERVER
            }

            is GeminiError.ClientError,
            is GeminiError.Unknown -> {
                LlmLogger.error(reqId, "❌ Fatal Error: ${e.message} errorType (${errorType::class.simpleName}) ")
                apiKeyProvider.reportKeyFailure(apiKeyId, errorType)
                ErrorAction.ROTATE_KEY
            }

            // Блокировка и Отмена обрабатываются здесь как фатальные для цикла, но по-разному логически
            is GeminiError.BlockedContent -> ErrorAction.FATAL // Блок контента обрабатывается внутри processResponse, тут редкость
            is GeminiError.Cancelled -> throw e
        }
    }

    private suspend fun getUserPremiumStatus(userId: Uuid): PremiumStatus {
        return dbQuery {
            AppUsers.select(AppUsers.premiumStatus)
                .where { AppUsers.id eq userId }
                .singleOrNull()
                ?.get(AppUsers.premiumStatus)
                ?: PremiumStatus.FREE
        }
    }

    // Вспомогательный Enum для управления потоком в цикле
    private enum class ErrorAction {
        ROTATE_KEY,
        RETRY_SERVER,
        FATAL
    }

    @Serializable
    private data class LlmJsonResponse(
        val text: String,
        val reaction: String? = null
    )
}

/**
 * Конвертирует наш внутренний список `DomainMessage` в формат, понятный для Google GenAI Java SDK.
 *
 * Эта функция инкапсулирует в себе всю логику подготовки контента:
 * - Использование `Content.builder()` для создания объектов.
 * - Правильное маппинг ролей.
 * - Обрезка слишком длинных сообщений (truncation) с логированием.
 */
private fun List<DomainMessage>.toGoogleContent(): List<Content> {

    return this
        // 1. ФИЛЬТР: Убираем системные сообщения и пустые сообщения
        .filter { it.role != MessageRole.SYSTEM && it.contents.isNotEmpty() }
        .mapNotNull { domainMessage -> // Используем mapNotNull на случай, если сообщение окажется пустым после проверок

            val role = when (domainMessage.role) {
                MessageRole.USER -> "user"
                MessageRole.MODEL -> "model"
                else -> "model"
            }

            val maxTextLength = if (domainMessage.role == MessageRole.USER) 5000 else 10000

            // Собираем финальное содержание сообщения из всех частей контента
            val combinedText = buildString {
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
                            // Саму картинку (URL) мы ИИ не шлем в текстовом режиме, 
                            // она будет дополнена текстовым описанием (visual description) 
                            // из другого куска контента в этом же сообщении (Type TEXT).
                            // Здесь просто помечаем факт отправки фото.
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

                // Добавляем реакции, если они есть
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

            if (combinedText.isBlank()) return@mapNotNull null

            var finalContent = combinedText
            if (finalContent.length > maxTextLength) {
                finalContent = finalContent.take(maxTextLength)
            }

            Content.builder()
                .role(role)
                .parts(listOf(Part.fromText(finalContent)))
                .build()
        }
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
