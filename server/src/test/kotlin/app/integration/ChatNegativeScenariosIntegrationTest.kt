package app.di

import app.application.chat.*
import app.apiv4.chat.ChatEventBus
import app.data.db.DatabaseFactory
import app.data.db.tables.aiCharacter.AICharacters
import app.data.db.tables.chat.MessageContents
import app.data.db.tables.chat.MessageSlots
import app.data.db.tables.chat.ChatSummaries
import app.data.db.tables.chatSessions.ChatSessions
import app.data.db.tables.LlmConfig
import app.data.db.tables.LlmProvider
import app.data.network.models.openai.*
import app.data.network.openai.OpenAiApiClient
import app.data.network.openai.OpenAiException
import app.data.repository.ChatRepository
import app.data.repository.UserAiSettingsRepository
import app.data.repository.UserRepository
import app.service.ImageGenerationResult
import app.service.ImageGenerationService
import app.service.chat.ChatSummaryService
import app.utils.dbQuery
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.shared.avalon.chat.models.MessageRole
import com.ensodai.shared.avalon.chat.models.Rarity
import com.ensodai.shared.avalon.chat.models.SystemEventType
import com.ensodai.shared.avalon.chat.models.TextPayload
import com.ensodai.shared.avalon.chat.services.MessageContentCodec
import com.ensodai.shared.avalon.chat.wss.ServerMessage
import com.ensodai.shared.avalon.gifts.models.GiftActionType
import com.ensodai.shared.avalon.gifts.models.PremiumStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import java.util.Collections
import kotlin.uuid.Uuid

class ChatNegativeScenariosIntegrationTest : KoinTest {

    private val adminUserId = Uuid.parse("8bd56bfb-54e2-4d44-b1c7-be6bc29cb99d")
    private val fakeOpenAiApiClient = FakeNegativeOpenAiClient()

    private val chatEventBus by inject<ChatEventBus>()
    private val chatContextLoader by inject<ChatContextLoader>()
    private val sendMessageChatCommandHandler by inject<SendMessageChatCommandHandler>()
    private val chatSummaryService by inject<ChatSummaryService>()
    private val imageGenerationService by inject<ImageGenerationService>()
    private val chatRepository by inject<ChatRepository>()
    private val userRepository by inject<UserRepository>()
    private val userRepositoryApp by inject<app.data.repository.UserRepositoryApp>()
    private val userAiSettingsRepository by inject<UserAiSettingsRepository>()

    companion object {
        @org.junit.jupiter.api.BeforeAll
        @JvmStatic
        fun initDb() {
            DatabaseFactory.init()
        }
    }

    @BeforeEach
    fun setup() {
        startKoin {
            allowOverride(true)
            modules(
                AppKoinModule().module(),
                PaymentModule().module(),
                TelegramModule().module(),
                SpecialBindingsModule().module(),
                module {
                    single<OpenAiApiClient> { fakeOpenAiApiClient }
                }
            )
        }
        kotlinx.coroutines.runBlocking {
            userRepositoryApp.updateFlowModeSettings(adminUserId, isFlowModeEnabled = false)
        }
    }

    @AfterEach
    fun teardown() {
        kotlinx.coroutines.runBlocking {
            userRepositoryApp.updateFlowModeSettings(adminUserId, isFlowModeEnabled = false)
        }
        stopKoin()
    }

    private suspend fun getOrCreateTestChatSession(): Triple<Uuid, Uuid, Uuid> {
        val characterId = dbQuery("findAnyCharacter") {
            AICharacters.selectAll().firstOrNull()?.get(AICharacters.id)?.value
        } ?: error("Character required in DB")

        val existingSession = dbQuery("findExistingSession") {
            ChatSessions.selectAll()
                .where { (ChatSessions.userId eq adminUserId) and (ChatSessions.characterId eq characterId) }
                .firstOrNull()
        }
        val chatSessionId = existingSession?.get(ChatSessions.id)?.value ?: Uuid.random()
        val chatVariantId = Uuid.random()

        if (existingSession == null) {
            chatRepository.createSessionAndVariant(
                userId = adminUserId,
                characterId = characterId,
                chatSessionId = chatSessionId,
                chatVariantId = chatVariantId
            )
        } else {
            chatRepository.createNewVariant(
                sessionId = chatSessionId,
                chatVariantId = chatVariantId
            )
        }

        return Triple(characterId, chatSessionId, chatVariantId)
    }

    @Test
    fun `test chat generation 402 insufficient credits saves system event in DB and emits to websocket`() = runBlocking {
        // 1. Настраиваем кастомного провайдера
        userAiSettingsRepository.saveOrUpdateSettings(
            userId = adminUserId,
            isCustomProviderEnabled = true,
            baseUrl = "https://openrouter.ai/api/v1",
            apiKey = "sk-or-v1-custom-key",
            textModel = "gpt-5-mini",
            imageModel = "dall-e-3",
            imagePromptModel = "gpt-5-mini",
            ttsModel = "tts-1",
            summaryModel = "gpt-5-mini"
        )

        val (characterId, chatSessionId, chatVariantId) = getOrCreateTestChatSession()

        // 2. Имитируем ошибку 402 от OpenRouter (нет денег на балансе)
        fakeOpenAiApiClient.chatCompletionResultProvider = {
            Result.failure(OpenAiException.InsufficientCredits("User has insufficient credits on OpenRouter", 402))
        }

        // 3. Подписываемся на события сокетов
        val receivedMessages = Collections.synchronizedList(mutableListOf<ServerMessage>())
        val subscriberJob = launch {
            chatEventBus.events.collect { userEvent ->
                if (userEvent.userId == adminUserId) {
                    receivedMessages.add(userEvent.message)
                }
            }
        }
        delay(100)

        // 4. Отправляем сообщение
        val actor = ChatActor(userId = adminUserId, sessionId = Uuid.random())
        val command = ChatCommand.SendMessage(
            slotId = Uuid.random(),
            contentId = Uuid.random(),
            containerId = Uuid.random(),
            chatVariantId = chatVariantId,
            chatSessionId = chatSessionId,
            contentType = AvalonMessageContentType.TEXT,
            text = "Тест ошибки баланса OpenRouter",
            messageSystemInstruction = null,
            characterId = characterId,
            llmModelCode = null
        )

        val loadResult = chatContextLoader.load(actor, command)
        val context = (loadResult as ChatContextLoadResult.Success).context

        sendMessageChatCommandHandler.handle(actor, command, context)
        delay(2500)

        // 5. ПРОВЕРЯЕМ БАЗУ ДАННЫХ: создалось ли системное сообщение CUSTOM_INSUFFICIENT_CREDITS
        val systemMessages = dbQuery("checkSystemEvent") {
            MessageContents.selectAll()
                .where { (MessageContents.type eq AvalonMessageContentType.SYSTEM_EVENT) and (MessageContents.content eq SystemEventType.CUSTOM_INSUFFICIENT_CREDITS.name) }
                .count()
        }
        assertTrue(systemMessages > 0L, "В БД должно быть записано системное сообщение CUSTOM_INSUFFICIENT_CREDITS")

        // 6. ПРОВЕРЯЕМ ВЕБСОКЕТ: пришло ли сообщение клиенту
        val hasSystemEventMessage = receivedMessages.any { msg ->
            msg is ServerMessage.NewChatMessage && msg.slot.containers.any { container ->
                container.contents.any { content -> content.content == SystemEventType.CUSTOM_INSUFFICIENT_CREDITS.name }
            }
        }
        assertTrue(hasSystemEventMessage, "Клиенту по WebSocket должно было прийти системное сообщение об отсутствии кредитов")

        subscriberJob.cancel()
    }

    @Test
    fun `test chat generation 404 model not found saves CUSTOM_MODEL_NOT_FOUND in DB`() = runBlocking {
        userAiSettingsRepository.saveOrUpdateSettings(
            userId = adminUserId,
            isCustomProviderEnabled = true,
            baseUrl = "https://openrouter.ai/api/v1",
            apiKey = "sk-or-v1-custom-key",
            textModel = "invalid-nonexistent-model",
            imageModel = "dall-e-3",
            imagePromptModel = "gpt-5-mini",
            ttsModel = "tts-1",
            summaryModel = "gpt-5-mini"
        )

        val (characterId, chatSessionId, chatVariantId) = getOrCreateTestChatSession()

        fakeOpenAiApiClient.chatCompletionResultProvider = {
            Result.failure(OpenAiException.ModelNotFound("Model invalid-nonexistent-model does not exist", 404))
        }

        val actor = ChatActor(userId = adminUserId, sessionId = Uuid.random())
        val command = ChatCommand.SendMessage(
            slotId = Uuid.random(),
            contentId = Uuid.random(),
            containerId = Uuid.random(),
            chatVariantId = chatVariantId,
            chatSessionId = chatSessionId,
            contentType = AvalonMessageContentType.TEXT,
            text = "Тест ошибки 404",
            messageSystemInstruction = null,
            characterId = characterId,
            llmModelCode = null
        )

        val loadResult = chatContextLoader.load(actor, command)
        val context = (loadResult as ChatContextLoadResult.Success).context

        sendMessageChatCommandHandler.handle(actor, command, context)
        delay(2500)

        val systemMessages = dbQuery("checkSystemEvent404") {
            MessageContents.selectAll()
                .where { (MessageContents.type eq AvalonMessageContentType.SYSTEM_EVENT) and (MessageContents.content eq SystemEventType.CUSTOM_MODEL_NOT_FOUND.name) }
                .count()
        }
        assertTrue(systemMessages > 0L, "В БД должно быть записано системное сообщение CUSTOM_MODEL_NOT_FOUND")
    }

    @Test
    fun `test chat generation missing api key results in CUSTOM_KEY_INVALID system event without network call`() = runBlocking {
        // Юзер включил кастомного провайдера, но стер ключ (пустая строка)
        userAiSettingsRepository.saveOrUpdateSettings(
            userId = adminUserId,
            isCustomProviderEnabled = true,
            baseUrl = "https://openrouter.ai/api/v1",
            apiKey = "",
            textModel = "gpt-5-mini",
            imageModel = "dall-e-3",
            imagePromptModel = "gpt-5-mini",
            ttsModel = "tts-1",
            summaryModel = "gpt-5-mini"
        )

        val (characterId, chatSessionId, chatVariantId) = getOrCreateTestChatSession()

        val actor = ChatActor(userId = adminUserId, sessionId = Uuid.random())
        val command = ChatCommand.SendMessage(
            slotId = Uuid.random(),
            contentId = Uuid.random(),
            containerId = Uuid.random(),
            chatVariantId = chatVariantId,
            chatSessionId = chatSessionId,
            contentType = AvalonMessageContentType.TEXT,
            text = "Попытка отправить без ключа",
            messageSystemInstruction = null,
            characterId = characterId,
            llmModelCode = null
        )

        val loadResult = chatContextLoader.load(actor, command)
        val context = (loadResult as ChatContextLoadResult.Success).context

        sendMessageChatCommandHandler.handle(actor, command, context)
        delay(500)

        // Проверяем системное сообщение
        val systemMessages = dbQuery("checkKeyInvalid") {
            MessageContents.selectAll()
                .where { (MessageContents.type eq AvalonMessageContentType.SYSTEM_EVENT) and (MessageContents.content eq SystemEventType.CUSTOM_KEY_INVALID.name) }
                .count()
        }
        assertTrue(systemMessages > 0L, "В БД должно быть создано сообщение CUSTOM_KEY_INVALID")
    }

    @Test
    fun `test summarization failure due to invalid summary model does not archive messages and preserves chat integrity`() = runBlocking {
        // 1. Настраиваем юзеру валидную модель текста, но битую модель саммаризации
        userAiSettingsRepository.saveOrUpdateSettings(
            userId = adminUserId,
            isCustomProviderEnabled = true,
            baseUrl = "https://openrouter.ai/api/v1",
            apiKey = "sk-or-v1-custom-key",
            textModel = "gpt-5-mini",
            imageModel = "dall-e-3",
            imagePromptModel = "gpt-5-mini",
            ttsModel = "tts-1",
            summaryModel = "broken-summary-model"
        )

        val (characterId, chatSessionId, chatVariantId) = getOrCreateTestChatSession()

        // 2. Создаем 55 сообщений в чате (порог для запуска саммаризации > 50)
        for (i in 1..55) {
            val slotId = Uuid.random()
            val isUser = (i % 2 != 0)
            val role = if (isUser) MessageRole.USER else MessageRole.MODEL
            chatRepository.createNewMessageTurn(
                messageRole = role,
                messageRarity = Rarity.COMMON,
                slotId = slotId,
                contentId = Uuid.random(),
                containerId = Uuid.random(),
                chatVariantId = chatVariantId,
                chatSessionId = chatSessionId,
                contentType = AvalonMessageContentType.TEXT,
                text = MessageContentCodec.encode(AvalonMessageContentType.TEXT, TextPayload(text = "Message $i")),
                messageSystemInstruction = null
            )
        }

        // 3. Настраиваем фейк: запрос саммаризации падает с 404
        fakeOpenAiApiClient.chatCompletionResultProvider = { req ->
            if (req.model == "broken-summary-model") {
                Result.failure(OpenAiException.ModelNotFound("Summary model not found", 404))
            } else {
                Result.success(OpenAiChatResponse(choices = listOf(OpenAiChoice(message = OpenAiChatMessage(role = "assistant", content = "OK")))))
            }
        }

        // 4. Запускаем проверку и саммаризацию
        chatSummaryService.launchCheckAndSummarize(chatVariantId, PremiumStatus.PREMIUM)
        delay(1500)

        // 5. ПРОВЕРЯЕМ:
        // А. Ни одно сообщение НЕ должно быть архивировано (isArchived = false)
        val archivedCount = dbQuery("checkArchivedCount") {
            MessageSlots.selectAll()
                .where { (MessageSlots.chatVariantId eq chatVariantId) and (MessageSlots.isArchived eq true) }
                .count()
        }
        assertEquals(0L, archivedCount, "При ошибке модели саммаризации сообщения НЕ должны архивироваться!")

        // Б. В таблице chat_summaries не должно появиться битых записей
        val summariesCount = dbQuery("checkSummariesCount") {
            ChatSummaries.selectAll()
                .where { ChatSummaries.chatVariantId eq chatVariantId }
                .count()
        }
        assertEquals(0L, summariesCount, "Не должно быть создано пустое или битое саммари")
    }

    @Test
    fun `test image generation safety censorship block results in SAFETY_VIOLATION error reason`() = runBlocking {
        userAiSettingsRepository.saveOrUpdateSettings(
            userId = adminUserId,
            isCustomProviderEnabled = true,
            baseUrl = "https://openrouter.ai/api/v1",
            apiKey = "sk-or-v1-custom-key",
            textModel = "gpt-5-mini",
            imageModel = "dall-e-3",
            imagePromptModel = "gpt-5-mini",
            ttsModel = "tts-1",
            summaryModel = "gpt-5-mini"
        )

        val (characterId, _) = getOrCreateTestChatSession()

        // 1. Шаг 1 (Анализ сцены) успешен
        // 2. Шаг 2 (Рендер промпта) успешен
        // 3. Шаг 3 (Генерация картинки) блокируется цензурой провайдера
        fakeOpenAiApiClient.chatCompletionResultProvider = { req ->
            if (req.messages.any { it.content?.contains("AI Scene Director") == true }) {
                Result.success(OpenAiChatResponse(choices = listOf(OpenAiChoice(message = OpenAiChatMessage(role = "assistant", content = """{"scenarioType":"DAILY_CASUAL","renderStyle":"SMARTPHONE_SNAPSHOT","cameraComposition":"FRONT_CAMERA_SELFIE","physicalDistance":"CONVERSATIONAL","characterAppearanceReference":"","userAppearanceReference":"","bodyPose":"","handsAction":"","facialExpression":"","sceneAtmosphere":"","wardrobeState":"","environmentLighting":"","otherPresentSubjects":[],"activeProp":"","overrideArtStyle":null}""")))))
            } else {
                Result.success(OpenAiChatResponse(choices = listOf(OpenAiChoice(message = OpenAiChatMessage(role = "assistant", content = """{"imagePrompt":"A photo of character","visualDescription":"A nice photo","era":"","archetype":""}""")))))
            }
        }

        fakeOpenAiApiClient.imageGenerationResultProvider = {
            Result.failure(OpenAiException.BadRequest("Safety filter triggered: prompt contains disallowed content"))
        }

        // Получаем DTO конфига персонажа
        val characterConfig = dbQuery("getCharacterDto") {
            val charRow = AICharacters.selectAll().where { AICharacters.id eq characterId }.single()
            app.data.models.AICharacterConfigLLMDto(
                characterId = characterId,
                userId = adminUserId,
                characterName = charRow[AICharacters.characterName],
                llmConfig = LlmConfig(
                    code = "gpt-5-mini",
                    name = "GPT-5 Mini",
                    modelAPIID = "gpt-5-mini",
                    provider = LlmProvider.OPENAI,
                    energyCost = 1,
                    minStatus = PremiumStatus.FREE,
                    isActive = true
                ),
                temperature = charRow[AICharacters.temperature],
                interactionFormat = charRow[AICharacters.interactionFormat],
                topK = charRow[AICharacters.topK],
                topP = charRow[AICharacters.topP],
                maxOutputTokens = charRow[AICharacters.maxOutputTokens],
                maxContextTokens = charRow[AICharacters.maxContextTokens],
                shortDescription = charRow[AICharacters.shortDescription],
                fullDescription = charRow[AICharacters.fullDescription],
                firstMessage = charRow[AICharacters.firstMessage],
                safetySettings = app.data.models.SafetySettingsDto(1, 1, 1, 1),
                globalSettings = app.data.models.GlobalSettingsLLMDto("rules", 1000),
                userPersona = null
            )
        }

        val imageResult = imageGenerationService.generateImage(
            aICharacterConfigLLMDto = characterConfig,
            giftName = "camera_selfie",
            history = emptyList(),
            actionType = GiftActionType.CAMERA_SELFIE
        )

        assertTrue(imageResult is ImageGenerationResult.Error, "Результат должен быть ошибкой")
        val errorResult = imageResult as ImageGenerationResult.Error
        assertEquals(ImageGenerationResult.Error.ErrorReason.SAFETY_VIOLATION, errorResult.reason, "Причина ошибки должна быть SAFETY_VIOLATION")
    }
}

class FakeNegativeOpenAiClient : OpenAiApiClient {
    var chatCompletionResultProvider: (OpenAiChatRequest) -> Result<OpenAiChatResponse> = {
        Result.success(OpenAiChatResponse(choices = listOf(OpenAiChoice(message = OpenAiChatMessage(role = "assistant", content = "Default fake response")))))
    }

    var imageGenerationResultProvider: (OpenAiImageRequest) -> Result<OpenAiImageResponse> = {
        Result.success(OpenAiImageResponse(data = listOf(OpenAiImage(b64Json = "aGVsbG8="))))
    }

    override suspend fun createChatCompletion(
        baseUrl: String,
        apiKey: String,
        request: OpenAiChatRequest,
        headers: Map<String, String>
    ): Result<OpenAiChatResponse> {
        return chatCompletionResultProvider(request)
    }

    override fun createChatCompletionStream(
        baseUrl: String,
        apiKey: String,
        request: OpenAiChatRequest,
        headers: Map<String, String>
    ): Flow<OpenAiStreamChunk> = emptyFlow()

    override suspend fun listModels(baseUrl: String, apiKey: String): Result<OpenAiModelsListResponse> {
        return Result.success(OpenAiModelsListResponse(data = emptyList()))
    }

    override suspend fun createImageGeneration(
        baseUrl: String,
        apiKey: String,
        request: OpenAiImageRequest,
        headers: Map<String, String>
    ): Result<OpenAiImageResponse> {
        return imageGenerationResultProvider(request)
    }

    override fun createSpeechStream(
        baseUrl: String,
        apiKey: String,
        request: OpenAiTtsRequest,
        headers: Map<String, String>
    ): Flow<ByteArray> = emptyFlow()
}
