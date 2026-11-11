package app.di

import app.application.chat.*
import app.apiv4.chat.ChatEventBus
import app.data.db.DatabaseFactory
import app.data.db.tables.aiCharacter.AICharacters
import app.data.db.tables.chatSessions.ChatSessions
import app.data.network.models.openai.*
import app.data.network.openai.OpenAiApiClient
import app.data.network.openai.OpenAiException
import app.data.repository.ChatRepository
import app.data.repository.UserAiSettingsRepository
import app.data.repository.UserRepository
import app.data.repository.UserRepositoryApp
import app.service.StreamingReactionFilter
import app.service.openai.OpenAiCompatibleClient
import app.utils.dbQuery
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.shared.avalon.chat.models.MessageRole
import com.ensodai.shared.avalon.chat.wss.ServerMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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

class FlowModeStreamingIntegrationTest : KoinTest {

    private val adminUserId = Uuid.parse("8bd56bfb-54e2-4d44-b1c7-be6bc29cb99d")
    private val fakeOpenAiApiClient = FakeStreamingOpenAiClient()

    private val chatEventBus by inject<ChatEventBus>()
    private val chatContextLoader by inject<ChatContextLoader>()
    private val sendMessageChatCommandHandler by inject<SendMessageChatCommandHandler>()
    private val chatRepository by inject<ChatRepository>()
    private val userRepositoryApp by inject<UserRepositoryApp>()
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
    fun `test Flow Mode streaming delivers chunks strips reactions and finalizes in DB and WebSockets`() = runBlocking {
        // 1. Включаем юзеру Flow Mode (потоковый режим) и кастомный OpenRouter ключ
        userRepositoryApp.updateFlowModeSettings(adminUserId, isFlowModeEnabled = true)
        userAiSettingsRepository.saveOrUpdateSettings(
            userId = adminUserId,
            providerId = com.ensodai.shared.avalon.user.models.AiProviderType.OPENROUTER,
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

        // 2. Настраиваем генерацию потоковых чанков от OpenRouter
        fakeOpenAiApiClient.streamChunksProvider = {
            flowOf(
                OpenAiStreamChunk(
                    choices = listOf(OpenAiStreamChoice(delta = OpenAiStreamDelta(content = "Привет! ")))
                ),
                OpenAiStreamChunk(
                    choices = listOf(OpenAiStreamChoice(delta = OpenAiStreamDelta(content = "Это тестовый стрим. <reaction>Улыбается</reaction>")))
                ),
                OpenAiStreamChunk(
                    choices = listOf(OpenAiStreamChoice(delta = OpenAiStreamDelta(content = ""))),
                    usage = OpenAiUsage(promptTokens = 10, completionTokens = 15, totalTokens = 25, cost = 0.00005)
                )
            )
        }

        // 3. Собираем все сообщения из WebSocket шины
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
            text = "Привет, протестируй мне Flow Mode!",
            messageSystemInstruction = null,
            characterId = characterId,
            llmModelCode = null
        )

        val loadResult = chatContextLoader.load(actor, command)
        val context = (loadResult as ChatContextLoadResult.Success).context

        sendMessageChatCommandHandler.handle(actor, command, context)

        // Даем время на генерацию и финализацию (ChatGenerationService debounce 1500ms + обработка)
        delay(3500)

        // 5. ПРОВЕРЯЕМ СООБЩЕНИЯ В WEBSOCKET:
        // А. Первым пришло NewChatMessage с пустым слотом
        val hasInitialSlot = receivedMessages.any { it is ServerMessage.NewChatMessage }
        assertTrue(hasInitialSlot, "Должен был прийти исходный пустой слот через NewChatMessage")

        // Б. Пришли MessageStreamChunk с дельтами текста
        val textChunks = receivedMessages.filterIsInstance<ServerMessage.MessageStreamChunk>().map { it.textChunk }
        assertTrue(textChunks.isNotEmpty(), "Должны были прийти потоковые чанки MessageStreamChunk")
        val joinedStreamedText = textChunks.joinToString("")
        assertTrue(joinedStreamedText.contains("Привет!"), "Чанк с 'Привет!' должен быть доставлен")
        assertTrue(joinedStreamedText.contains("Это тестовый стрим."), "Чанк с 'Это тестовый стрим.' должен быть доставлен")
        
        // ВАЖНО: Тег реакции НЕ должен попасть в текстовый поток клиента!
        assertFalse(joinedStreamedText.contains("<reaction>"), "Тег <reaction> должен быть вырезан из текстовых чанков!")

        // В. Пришло событие финализации MessageUpdated
        val hasMessageUpdated = receivedMessages.any { it is ServerMessage.MessageUpdated && it.isLastMessage }
        assertTrue(hasMessageUpdated, "Должно было прийти событие финализации MessageUpdated")

        // Г. Пришла реакция персонажа в сокеты
        val reactionEvent = receivedMessages.filterIsInstance<ServerMessage.SendCharacterReaction>().firstOrNull()
        assertNotNull(reactionEvent, "Должно было прийти событие SendCharacterReaction")
        assertEquals("Улыбается", reactionEvent?.reaction)

        // 6. ПРОВЕРЯЕМ БАЗУ ДАННЫХ:
        // Итоговый текст в БД должен быть чистым (без тега реакции)
        val latestHistory = chatRepository.getHistoryForVariant(variantId = chatVariantId, limit = 5)
        val aiMessage = latestHistory.messages.lastOrNull { it.role == MessageRole.MODEL }
        assertNotNull(aiMessage, "В истории должно появиться сообщение от MODEL")
        
        val aiContent = aiMessage!!.contents.firstOrNull()?.content.orEmpty()
        assertTrue(aiContent.contains("Привет! Это тестовый стрим."), "Финальный текст в БД должен быть сохранен полностью")
        assertFalse(aiContent.contains("<reaction>"), "В БД текст должен быть очищен от тега реакции")
        
        // Реакция персонажа вешается на сообщение юзера, на которое ответил персонаж
        val userMessage = latestHistory.messages.lastOrNull { it.role == MessageRole.USER }
        assertNotNull(userMessage, "В истории должно быть сообщение от USER")
        assertEquals("Улыбается", userMessage!!.characterReaction, "Реакция персонажа должна быть сохранена на сообщении пользователя")

        subscriberJob.cancel()
    }

    @Test
    fun `test StreamingReactionFilter parses reactions split across multiple chunks`() {
        val filter = StreamingReactionFilter()

        // Чанк 1: начало текста
        val (text1, reaction1) = filter.process("Привет, путник! <re")
        assertEquals("Привет, путник! ", text1)
        assertNull(reaction1)

        // Чанк 2: достройка открывающего тега и начало реакции
        val (text2, reaction2) = filter.process("action>Сме")
        assertEquals("", text2, "Тег и содержимое реакции не должны попадать в текст")
        assertNull(reaction2)

        // Чанк 3: окончание реакции и закрывающий тег + хвост текста
        val (text3, reaction3) = filter.process("ется</reaction> Рад видеть тебя.")
        assertEquals(" Рад видеть тебя.", text3)
        assertEquals("Смеется", reaction3, "Реакция должна быть извлечена полностью")

        // Завершение стрима
        val flushed = filter.flush()
        assertEquals("", flushed)
    }

    @Test
    fun `test OpenAiCompatibleClient streaming correctly accumulates tokens and cost`() = runBlocking {
        val client = OpenAiCompatibleClient(fakeOpenAiApiClient)
        fakeOpenAiApiClient.streamChunksProvider = {
            flowOf(
                OpenAiStreamChunk(choices = listOf(OpenAiStreamChoice(delta = OpenAiStreamDelta(content = "Раз, ")))),
                OpenAiStreamChunk(choices = listOf(OpenAiStreamChoice(delta = OpenAiStreamDelta(content = "два, ")))),
                OpenAiStreamChunk(
                    choices = listOf(OpenAiStreamChoice(delta = OpenAiStreamDelta(content = "три!"))),
                    usage = OpenAiUsage(promptTokens = 12, completionTokens = 8, totalTokens = 20, cost = 0.0001)
                )
            )
        }

        val chunks = client.generateChatCompletionStream(
            baseUrl = "https://openrouter.ai/api/v1",
            apiKey = "sk-test",
            request = OpenAiChatRequest(model = "gpt-5-mini", messages = emptyList())
        ).toList()

        assertEquals(4, chunks.size, "3 текстовых чанка + 1 финальный чанк с tokenStats")
        
        val fullText = chunks.filter { !it.isFinished }.joinToString("") { it.textDelta }
        assertEquals("Раз, два, три!", fullText)

        val finalChunk = chunks.last()
        assertTrue(finalChunk.isFinished)
        assertNotNull(finalChunk.tokenStats)
        assertEquals(20L, finalChunk.tokenStats?.totalTokens)
        assertEquals(0, java.math.BigDecimal("0.0001").compareTo(finalChunk.tokenStats?.openRouterCostUsd))
    }
}

class FakeStreamingOpenAiClient : OpenAiApiClient {
    var streamChunksProvider: () -> Flow<OpenAiStreamChunk> = { emptyFlow() }

    override fun createChatCompletionStream(
        baseUrl: String,
        apiKey: String,
        request: OpenAiChatRequest,
        headers: Map<String, String>
    ): Flow<OpenAiStreamChunk> {
        return streamChunksProvider()
    }

    override suspend fun createChatCompletion(
        baseUrl: String,
        apiKey: String,
        request: OpenAiChatRequest,
        headers: Map<String, String>
    ): Result<OpenAiChatResponse> {
        return Result.success(OpenAiChatResponse(choices = listOf(OpenAiChoice(message = OpenAiChatMessage(role = "assistant", content = "OK")))))
    }

    override suspend fun listModels(baseUrl: String, apiKey: String): Result<OpenAiModelsListResponse> {
        return Result.success(OpenAiModelsListResponse(data = emptyList()))
    }

    override suspend fun createImageGeneration(
        baseUrl: String,
        apiKey: String,
        request: OpenAiImageRequest,
        headers: Map<String, String>
    ): Result<OpenAiImageResponse> {
        return Result.failure(NotImplementedError())
    }

    override fun createSpeechStream(
        baseUrl: String,
        apiKey: String,
        request: OpenAiTtsRequest,
        headers: Map<String, String>
    ): Flow<ByteArray> = emptyFlow()
}
