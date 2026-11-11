package app.di

import app.application.chat.*
import app.apiv4.chat.ChatEventBus
import app.domain.chat.GenerateVariantFlow
import app.data.db.DatabaseFactory
import app.data.db.tables.aiCharacter.AICharacters
import app.data.db.tables.chatSessions.ChatSessions
import app.data.network.models.openai.*
import app.data.network.openai.OpenAiApiClient
import app.data.repository.ChatRepository
import app.data.repository.UserAiSettingsRepository
import app.data.repository.UserRepository
import app.utils.dbQuery
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.shared.avalon.chat.models.MessageRole
import com.ensodai.shared.avalon.chat.models.Rarity
import com.ensodai.shared.avalon.chat.models.TextPayload
import com.ensodai.shared.avalon.chat.services.MessageContentCodec
import com.ensodai.shared.avalon.chat.wss.ServerMessage
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

class FullChatPipelineIntegrationTest : KoinTest {

    private val adminUserId = Uuid.parse("8bd56bfb-54e2-4d44-b1c7-be6bc29cb99d")
    private val fakeOpenAiApiClient = FakeChatOpenAiClient()

    private val chatEventBus by inject<ChatEventBus>()
    private val chatContextLoader by inject<ChatContextLoader>()
    private val sendMessageChatCommandHandler by inject<SendMessageChatCommandHandler>()
    private val generateVariantFlow by inject<GenerateVariantFlow>()
    private val forceGenerateFlow by inject<app.domain.chat.ForceGenerateFlow>()
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

    @Test
    fun `test full chat pipeline with custom OpenRouter provider persists message triggers AI and broadcasts via ChatEventBus`() = runBlocking {
        // 1. Убеждаемся, что админ есть в базе
        val user = userRepository.findById(adminUserId)
        assertNotNull(user, "User $adminUserId must exist in local DB")

        // 2. Включаем ему кастомный OpenRouter провайдер
        userAiSettingsRepository.saveOrUpdateSettings(
            userId = adminUserId,
            isCustomProviderEnabled = true,
            baseUrl = "https://openrouter.ai/api/v1",
            apiKey = "sk-or-v1-custom-test-key",
            textModel = "gpt-5-mini",
            imageModel = "dall-e-3",
            imagePromptModel = "gpt-5-mini",
            ttsModel = "tts-1",
            summaryModel = "gpt-5-mini"
        )

        // 3. Ищем существующего персонажа в базе
        val characterId = dbQuery("findAnyCharacter") {
            AICharacters.selectAll().firstOrNull()?.get(AICharacters.id)?.value
        }
        assertNotNull(characterId, "В локальной базе должен быть хотя бы один персонаж AICharacters для чата!")

        // 4. Идемпотентно получаем/создаем сессию чата и новую ветку
        val existingSession = dbQuery("findExistingSession") {
            ChatSessions.selectAll()
                .where { (ChatSessions.userId eq adminUserId) and (ChatSessions.characterId eq characterId!!) }
                .firstOrNull()
        }
        val chatSessionId = existingSession?.get(ChatSessions.id)?.value ?: Uuid.random()
        val chatVariantId = Uuid.random()

        if (existingSession == null) {
            chatRepository.createSessionAndVariant(
                userId = adminUserId,
                characterId = characterId!!,
                chatSessionId = chatSessionId,
                chatVariantId = chatVariantId
            )
        } else {
            chatRepository.createNewVariant(
                sessionId = chatSessionId,
                chatVariantId = chatVariantId
            )
        }

        // 5. Настраиваем фейковый ответ нейронки
        fakeOpenAiApiClient.chatCompletionResult = Result.success(
            OpenAiChatResponse(
                choices = listOf(
                    OpenAiChoice(
                        message = OpenAiChatMessage(role = "assistant", content = "Привет! Я сгенерированный ИИ-ответ.")
                    )
                ),
                usage = OpenAiUsage(totalTokens = 35, cost = 0.00008)
            )
        )

        // 6. Подписываемся на события вебсокетов (ChatEventBus)
        val receivedMessages = Collections.synchronizedList(mutableListOf<ServerMessage>())
        val subscriberJob = launch {
            chatEventBus.events.collect { userEvent ->
                if (userEvent.userId == adminUserId) {
                    receivedMessages.add(userEvent.message)
                }
            }
        }

        delay(100)

        // 7. Создаем команду отправки сообщения от юзера
        val clientSessionId = Uuid.random()
        val actor = ChatActor(userId = adminUserId, sessionId = clientSessionId)
        val command = ChatCommand.SendMessage(
            slotId = Uuid.random(),
            contentId = Uuid.random(),
            containerId = Uuid.random(),
            chatVariantId = chatVariantId,
            chatSessionId = chatSessionId,
            contentType = AvalonMessageContentType.TEXT,
            text = "Привет, персонаж! Это тестовое сообщение юзера.",
            messageSystemInstruction = null,
            characterId = characterId!!,
            llmModelCode = null
        )

        // Загружаем контекст команды
        val loadResult = chatContextLoader.load(actor, command)
        assertTrue(loadResult is ChatContextLoadResult.Success, "Контекст чата должен успешно загрузиться: $loadResult")
        val context = (loadResult as ChatContextLoadResult.Success).context

        val initialBalance = userRepository.getUserEnergyBalance(adminUserId)

        // 8. ЗАПУСКАЕМ ПОЛНЫЙ ПАЙПЛАЙН ОТПРАВКИ СООБЩЕНИЯ
        sendMessageChatCommandHandler.handle(
            actor = actor,
            command = command,
            context = context
        )

        delay(1500)

        // 9. ПРОВЕРЯЕМ РЕЗУЛЬТАТЫ В ВЕБСОКЕТАХ
        val hasAck = receivedMessages.any { it is ServerMessage.MessageAck }
        assertTrue(hasAck, "В шину должен был улететь ServerMessage.MessageAck!")

        // 10. ПРОВЕРЯЕМ СОХРАНЕНИЕ В БАЗУ ДАННЫХ
        val history = chatRepository.getHistoryForVariant(
            variantId = chatVariantId,
            limit = 10
        )
        assertTrue(history.messages.isNotEmpty(), "История сообщений в БД не должна быть пустой!")
        
        // 11. ПРОВЕРЯЕМ ЭНЕРГИЮ ЮЗЕРА
        val finalBalance = userRepository.getUserEnergyBalance(adminUserId)
        assertEquals(initialBalance.dailyEnergy, finalBalance.dailyEnergy, "Энергия не должна списываться при кастомном ключе!")

        subscriberJob.cancel()
    }

    @Test
    fun `test swipe message regeneration sends exact preceding history to LLM and appends new variant container`() = runBlocking {
        // 1. Включаем кастомный OpenRouter провайдер
        userAiSettingsRepository.saveOrUpdateSettings(
            userId = adminUserId,
            isCustomProviderEnabled = true,
            baseUrl = "https://openrouter.ai/api/v1",
            apiKey = "sk-or-v1-custom-test-key",
            textModel = "gpt-5-mini",
            imageModel = "dall-e-3",
            imagePromptModel = "gpt-5-mini",
            ttsModel = "tts-1",
            summaryModel = "gpt-5-mini"
        )

        // 2. Ищем персонажа
        val characterId = dbQuery("findAnyCharacter") {
            AICharacters.selectAll().firstOrNull()?.get(AICharacters.id)?.value
        }
        assertNotNull(characterId, "Character is required")

        // 3. Идемпотентно получаем/создаем сессию чата и новую ветку
        val existingSession = dbQuery("findExistingSession") {
            ChatSessions.selectAll()
                .where { (ChatSessions.userId eq adminUserId) and (ChatSessions.characterId eq characterId!!) }
                .firstOrNull()
        }
        val chatSessionId = existingSession?.get(ChatSessions.id)?.value ?: Uuid.random()
        val chatVariantId = Uuid.random()

        if (existingSession == null) {
            chatRepository.createSessionAndVariant(
                userId = adminUserId,
                characterId = characterId!!,
                chatSessionId = chatSessionId,
                chatVariantId = chatVariantId
            )
        } else {
            chatRepository.createNewVariant(
                sessionId = chatSessionId,
                chatVariantId = chatVariantId
            )
        }

        // 3. Заполняем чат 10 сообщениями (5 от юзера, 5 от ИИ)
        val slotIds = mutableListOf<Uuid>()
        for (i in 1..10) {
            val slotId = Uuid.random()
            slotIds.add(slotId)
            val isUser = (i % 2 != 0)
            val role = if (isUser) MessageRole.USER else MessageRole.MODEL
            val encodedText = MessageContentCodec.encode(
                type = AvalonMessageContentType.TEXT,
                payload = TextPayload(text = "Message index $i from $role")
            )

            chatRepository.createNewMessageTurn(
                messageRole = role,
                messageRarity = Rarity.COMMON,
                slotId = slotId,
                contentId = Uuid.random(),
                containerId = Uuid.random(),
                chatVariantId = chatVariantId,
                chatSessionId = chatSessionId,
                contentType = AvalonMessageContentType.TEXT,
                text = encodedText,
                messageSystemInstruction = null
            )
            delay(10)
        }

        val lastAiSlotId = slotIds.last() // 10-е сообщение (от MODEL)

        // 4. Настраиваем фейк OpenRouter на возврат нового варианта
        fakeOpenAiApiClient.chatCompletionResult = Result.success(
            OpenAiChatResponse(
                choices = listOf(
                    OpenAiChoice(
                        message = OpenAiChatMessage(role = "assistant", content = "Это сгенерированный вариант номер 2!")
                    )
                ),
                usage = OpenAiUsage(totalTokens = 45, cost = 0.0001)
            )
        )

        // 5. ДЕЛАЕМ СВАЙП (запускаем генерацию нового варианта для 10-го слота)
        generateVariantFlow.run(
            userId = adminUserId,
            chatSessionId = chatSessionId,
            chatVariantId = chatVariantId,
            characterId = characterId!!,
            slotId = lastAiSlotId,
            llmModelCode = null
        )

        // Ждем асинхронную генерацию (ChatGenerationService имеет debounce 1500ms)
        delay(2500)

        // 6. ПРОВЕРЯЕМ, ЧТО УШЛО В НЕЙРОНКУ
        val lastRequest = fakeOpenAiApiClient.lastChatRequest
        assertNotNull(lastRequest, "Запрос к LLM должен был быть отправлен!")
        
        // В истории сообщений не должно быть текста 10-го сообщения ("Message index 10")
        val sentHistoryText = lastRequest!!.messages.joinToString(" ") { it.content.orEmpty() }
        assertFalse(sentHistoryText.contains("Message index 10"), "Старый текст свайпаемого сообщения НЕ должен уходить в историю!")
        
        // При этом предшествующие сообщения (например 9-е) должны присутствовать в промпте
        assertTrue(sentHistoryText.contains("Message index 9"), "Предшествующие сообщения диалога обязаны присутствовать в истории!")

        // 7. ПРОВЕРЯЕМ БАЗУ ДАННЫХ: у последнего слота теперь должно быть 2 контейнера (варианта)
        val containersCount = dbQuery("countContainersForSlot") {
            app.data.db.tables.chat.MessageContainers.selectAll()
                .where { app.data.db.tables.chat.MessageContainers.slotId eq lastAiSlotId }
                .count()
        }
        assertEquals(2L, containersCount, "У слота после свайпа должно стать ровно 2 контейнера (варианта)!")
    }

    @Test
    fun `test force start generation creates new turn without user message and persists AI response`() = runBlocking {
        // 1. Включаем кастомный OpenRouter провайдер
        userAiSettingsRepository.saveOrUpdateSettings(
            userId = adminUserId,
            isCustomProviderEnabled = true,
            baseUrl = "https://openrouter.ai/api/v1",
            apiKey = "sk-or-v1-custom-test-key",
            textModel = "gpt-5-mini",
            imageModel = "dall-e-3",
            imagePromptModel = "gpt-5-mini",
            ttsModel = "tts-1",
            summaryModel = "gpt-5-mini"
        )

        // 2. Ищем персонажа
        val characterId = dbQuery("findAnyCharacter") {
            AICharacters.selectAll().firstOrNull()?.get(AICharacters.id)?.value
        }
        assertNotNull(characterId, "Character is required")

        // 3. Идемпотентно получаем/создаем сессию чата и новую ветку
        val existingSession = dbQuery("findExistingSession") {
            ChatSessions.selectAll()
                .where { (ChatSessions.userId eq adminUserId) and (ChatSessions.characterId eq characterId!!) }
                .firstOrNull()
        }
        val chatSessionId = existingSession?.get(ChatSessions.id)?.value ?: Uuid.random()
        val chatVariantId = Uuid.random()

        if (existingSession == null) {
            chatRepository.createSessionAndVariant(
                userId = adminUserId,
                characterId = characterId!!,
                chatSessionId = chatSessionId,
                chatVariantId = chatVariantId
            )
        } else {
            chatRepository.createNewVariant(
                sessionId = chatSessionId,
                chatVariantId = chatVariantId
            )
        }

        // 4. Заполняем чат 4 сообщениями (2 юзер, 2 ИИ)
        for (i in 1..4) {
            val isUser = (i % 2 != 0)
            val role = if (isUser) MessageRole.USER else MessageRole.MODEL
            val encodedText = MessageContentCodec.encode(
                type = AvalonMessageContentType.TEXT,
                payload = TextPayload(text = "Initial message $i from $role")
            )

            chatRepository.createNewMessageTurn(
                messageRole = role,
                messageRarity = Rarity.COMMON,
                slotId = Uuid.random(),
                contentId = Uuid.random(),
                containerId = Uuid.random(),
                chatVariantId = chatVariantId,
                chatSessionId = chatSessionId,
                contentType = AvalonMessageContentType.TEXT,
                text = encodedText,
                messageSystemInstruction = null
            )
            delay(10)
        }

        // Проверяем, что в истории изначально ровно 4 сообщения
        val initialHistory = chatRepository.getHistoryForVariant(variantId = chatVariantId, limit = 10)
        assertEquals(4, initialHistory.messages.size, "До force generate должно быть 4 сообщения")

        // 5. Настраиваем ответ нейросети на force generate
        fakeOpenAiApiClient.chatCompletionResult = Result.success(
            OpenAiChatResponse(
                choices = listOf(
                    OpenAiChoice(
                        message = OpenAiChatMessage(role = "assistant", content = "Я продолжил диалог сам без нового сообщения!")
                    )
                ),
                usage = OpenAiUsage(totalTokens = 50, cost = 0.00015)
            )
        )

        // 6. ЗАПУСКАЕМ FORCE GENERATION (юзер не отправлял текст, а нажал 'Продолжить / Force Generate')
        forceGenerateFlow.run(
            userId = adminUserId,
            chatSessionId = chatSessionId,
            chatVariantId = chatVariantId,
            characterId = characterId!!,
            llmModelCode = null
        )

        // Ждем асинхронную генерацию (ChatGenerationService debounce 1500ms + обработка)
        delay(2500)

        // 7. ПРОВЕРЯЕМ ЗАПРОС К НЕЙРОСЕТИ
        val lastRequest = fakeOpenAiApiClient.lastChatRequest
        assertNotNull(lastRequest, "Запрос к LLM при force generation должен был отправиться!")
        val sentHistory = lastRequest!!.messages.joinToString(" ") { it.content.orEmpty() }
        assertTrue(sentHistory.contains("Initial message 4"), "Вся текущая история должна уйти в модель для продолжения!")

        // 8. ПРОВЕРЯЕМ БАЗУ ДАННЫХ: появился НОВЫЙ 5-й ход (сообщение от MODEL)
        val historyAfterForce = chatRepository.getHistoryForVariant(variantId = chatVariantId, limit = 10)
        assertEquals(5, historyAfterForce.messages.size, "В базе должен появиться новый 5-й ход диалога!")
        
        val lastMessage = historyAfterForce.messages.last()
        assertEquals(MessageRole.MODEL, lastMessage.role, "Последний сгенерированный ход должен быть от MODEL")
        val decodedContent = lastMessage.contents.firstOrNull()?.content.orEmpty()
        assertTrue(decodedContent.contains("Я продолжил диалог сам без нового сообщения!"), "Текст ответа должен сохраниться в БД")
    }
}

class FakeChatOpenAiClient : OpenAiApiClient {
    var chatCompletionResult: Result<OpenAiChatResponse>? = null
    var lastChatRequest: OpenAiChatRequest? = null

    override suspend fun createChatCompletion(
        baseUrl: String,
        apiKey: String,
        request: OpenAiChatRequest,
        headers: Map<String, String>
    ): Result<OpenAiChatResponse> {
        lastChatRequest = request
        return chatCompletionResult ?: error("chatCompletionResult was not set in test!")
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
        return Result.failure(NotImplementedError())
    }

    override fun createSpeechStream(
        baseUrl: String,
        apiKey: String,
        request: OpenAiTtsRequest,
        headers: Map<String, String>
    ): Flow<ByteArray> = emptyFlow()
}
