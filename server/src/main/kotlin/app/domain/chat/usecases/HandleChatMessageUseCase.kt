package app.domain.chat.usecases

import app.application.chat.ChatActor
import app.application.chat.ChatCommand
import app.application.chat.ChatCommandContext
import app.application.chat.ChatContextMode
import app.application.chat.PreparedSendMessageContext
import app.data.db.tables.LlmConfig
import app.data.db.tables.chatSessions.ChatVariants
import app.data.models.AICharacterConfigLLMDto
import app.data.repository.UserRepository
import app.domain.chat.ChatContextService
import app.domain.chat.ChatGenerationService
import app.domain.chat.ForceGenerateFlow
import app.domain.chat.GenerateVariantFlow
import app.domain.chat.SendMessageFlow
import com.ensodai.shared.avalon.chat.wss.ServerMessage
import app.service.LlmConfigService
import app.service.chat.MessageNotifier
import app.utils.dbQuery
import app.utils.getUTCTimeNow
import app.utils.logger.WssLogger
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.update
import org.koin.core.annotation.Single
import java.util.concurrent.TimeUnit
import kotlin.uuid.Uuid

/**
 * Основной UseCase для обработки входящего сообщения от пользователя.
 * Запускает полную цепочку: проверка квот, сохранение, подготовка истории,
 * вызов LLM, обработка результата и отправка уведомлений.
 * Является точкой входа для всей бизнес-логики чата.
 */
@Single
class HandleChatMessageUseCase(
    private val messageNotifier: MessageNotifier,
    private val llmConfigService: LlmConfigService,
    private val chatContextService: ChatContextService,
    private val chatGenerationService: ChatGenerationService,
    private val sendMessageFlow: SendMessageFlow,
    private val forceGenerateFlow: ForceGenerateFlow,
    private val generateVariantFlow: GenerateVariantFlow,
    private val userRepository: UserRepository
) {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Если к мьютексу (чату) не обращались 1 час — он удаляется из памяти.
    private val chatMutexes = Caffeine.newBuilder()
        .expireAfterAccess(1, TimeUnit.HOURS)
        .build<Uuid, Mutex>()

    /**
     * Получает существующий мьютекс или создает новый для этой сессии.
     */
    private fun getMutex(sessionId: Uuid): Mutex {
        return chatMutexes.get(sessionId) { Mutex() }
    }

    /**
     * Остановка генерации.
     */
    fun stop(chatSessionId: Uuid) {
        WssLogger.info("Стоп генерации")
        chatGenerationService.stop(chatSessionId)
    }

    /**
     * Регенерация (Force Start).
     * Отличается от start тем, что мы НЕ сохраняем сообщение юзера,
     * а сразу переходим к генерации ответа на основе истории.
     */
    fun forceStart(
        userId: Uuid,
        chatSessionId: Uuid,
        chatVariantId: Uuid,
        characterId: Uuid,
        llmModelCode: String?
    ) {
        WssLogger.info("Старт форсированной генерации")
        applicationScope.launch {
            forceGenerateFlow.run(
                userId = userId,
                chatSessionId = chatSessionId,
                chatVariantId = chatVariantId,
                characterId = characterId,
                llmModelCode = llmModelCode
            )
        }
    }

    fun generateVariant(
        userId: Uuid,
        chatSessionId: Uuid,
        chatVariantId: Uuid,
        characterId: Uuid,
        slotId: Uuid,
        llmModelCode: String?
    ) {
        WssLogger.info("[HandleChatMessageUseCase] Запуск generateVariant: userId=$userId, slotId=$slotId")
        applicationScope.launch {
            try {
                WssLogger.info("[HandleChatMessageUseCase] Вызов generateVariantFlow.run")
                generateVariantFlow.run(
                    userId = userId,
                    chatSessionId = chatSessionId,
                    chatVariantId = chatVariantId,
                    characterId = characterId,
                    slotId = slotId,
                    llmModelCode = llmModelCode
                )
                WssLogger.info("[HandleChatMessageUseCase] generateVariantFlow.run успешно завершился")
            } catch (e: Throwable) {
                WssLogger.fail("[HandleChatMessageUseCase] Ошибка во время выполнения generateVariantFlow: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /**
     * Стандартная отправка сообщения.
     * 1. Сохраняем User Message.
     * 2. Шлем ACK.
     * 3. Запускаем генерацию.
     */
    fun start(
        userId: Uuid,
        wssUserSessionId: Uuid,
        slotId: Uuid,
        contentId: Uuid,
        containerId: Uuid,
        chatVariantId: Uuid,
        chatSessionId: Uuid,
        contentType: AvalonMessageContentType,
        text: String,
        characterId: Uuid,
        llmModelCode: String?,
        messageSystemInstruction: String?
    ) {
        WssLogger.info("Старт генерации")
        applicationScope.launch {
            val mutex = getMutex(chatSessionId)
            mutex.withLock {
                try {
                    val userPremiumStatus = userRepository.getUserPremiumStatus(userId)
                    val llmConfig = llmConfigService.getConfig(llmModelCode, userPremiumStatus)
                    WssLogger.info("Валидация контекста")
                    val characterConfig = chatContextService.getOrCreateContext(
                        userId = userId,
                        chatSessionId = chatSessionId,
                        chatVariantId = chatVariantId,
                        characterId = characterId,
                        llmConfig = llmConfig
                    ) ?: return@launch
                    sendMessageFlow.run(
                        prepared = buildPreparedSendMessageContext(
                            userId = userId,
                            sessionId = wssUserSessionId,
                            slotId = slotId,
                            contentId = contentId,
                            containerId = containerId,
                            chatVariantId = chatVariantId,
                            chatSessionId = chatSessionId,
                            contentType = contentType,
                            text = text,
                            messageSystemInstruction = messageSystemInstruction,
                            characterId = characterId,
                            llmModelCode = llmModelCode,
                            llmConfig = llmConfig,
                            characterConfig = characterConfig
                        )
                    )
                } catch (e: Exception) {
                    // Обработка ошибок валидации или сохранения user message
                    WssLogger.fail("CRITICAL ERROR in start(): ${e.localizedMessage}")
                    val messageAck = ServerMessage.MessageAckError(
                        containerId = containerId,
                        serverId = containerId,
                        timestamp = getUTCTimeNow(),
                        slotId = slotId
                    )
                    messageNotifier.notifyUserWithMessage(
                        userId = userId,
                        message = messageAck,
                        excludeSessionId = null
                    )
//                notifyError(userId, e)
                }
            }
        }
    }

    private fun buildPreparedSendMessageContext(
        userId: Uuid,
        sessionId: Uuid,
        slotId: Uuid,
        contentId: Uuid,
        containerId: Uuid,
        chatVariantId: Uuid,
        chatSessionId: Uuid,
        contentType: AvalonMessageContentType,
        text: String,
        messageSystemInstruction: String?,
        characterId: Uuid,
        llmModelCode: String?,
        llmConfig: LlmConfig,
        characterConfig: AICharacterConfigLLMDto
    ): PreparedSendMessageContext {
        val actor = ChatActor(
            userId = userId,
            sessionId = sessionId
        )
        val command = ChatCommand.SendMessage(
            slotId = slotId,
            contentId = contentId,
            containerId = containerId,
            chatVariantId = chatVariantId,
            chatSessionId = chatSessionId,
            contentType = contentType,
            text = text,
            messageSystemInstruction = messageSystemInstruction,
            characterId = characterId,
            llmModelCode = llmModelCode
        )

        return PreparedSendMessageContext(
            actor = actor,
            command = command,
            chatContext = ChatCommandContext(
                actor = actor,
                command = command,
                session = null,
                variant = null,
                mode = ChatContextMode.BOOTSTRAP_ALLOWED
            ),
            llmConfig = llmConfig,
            characterConfig = characterConfig
        )
    }

}