package app.domain.chat
import org.koin.core.annotation.Single
import com.ensodai.shared.avalon.chat.wss.ServerMessage

import app.data.db.tables.LlmConfig
import app.data.models.AICharacterConfigLLMDto
import app.service.chat.CharacterLLMConfigService
import app.data.repository.ChatRepository
import app.data.repository.UserRepository
import app.service.chat.MessageNotifier
import app.utils.logger.WssLogger
import kotlin.uuid.Uuid

/**
 * Сервис отвечает за подготовку контекста чата.
 * Реализует паттерн Lazy Creation: если чат не найден, пытается его создать "на лету".
 */
@Single
class ChatContextService(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val characterLLMConfigService: CharacterLLMConfigService,
    private val messageNotifier: MessageNotifier
) {

    /**
     * Пытается найти конфигурацию для генерации.
     * Если сессия/вариант отсутствуют в БД — создает их и пробует снова.
     * В случае фатальных ошибок (юзер не найден, конфиг не создался) — шлет уведомление об ошибке.
     */
    suspend fun getOrCreateContext(
        userId: Uuid,
        chatSessionId: Uuid,
        chatVariantId: Uuid,
        characterId: Uuid,
        llmConfig: LlmConfig
    ): AICharacterConfigLLMDto? {

        // 1. Проверка юзера (Legacy check из твоего кода)
        // WssLogger.info("Поиск юзера") -> можно оставить логи внутри сервисов если нужно
        val user = userRepository.findById(userId)
        if (user == null) {
            WssLogger.fail("Internal error: User not found")
            messageNotifier.notifyUserWithError(userId, "Internal error: User not found")
            return null
        }

        // 2. ПОПЫТКА №1: Ищем конфиг (обычный сценарий)
        WssLogger.info("Попытка получения конфига для VariantID: $chatVariantId")
        var characterLLMConfig = characterLLMConfigService.get(
            id = chatVariantId,
            llmConfig = llmConfig
        )
        WssLogger.info("Попытка получения саммари для VariantID: $chatVariantId")
        // Временно закомментировано суммаризирование (memory)
//        val summary = chatRepository.getActiveSummary(chatVariantId)
        val summary: String? = null // chatRepository.getActiveSummary(chatVariantId)
        // 3. ЕСЛИ НЕ НАШЛИ -> LAZY CREATION
        if (characterLLMConfig == null) {
            WssLogger.info("⚡️ Lazy Creation: Чат не найден, пробуем создать...")

            chatRepository.createSessionAndVariant(
                userId = userId,
                characterId = characterId,
                chatSessionId = chatSessionId,
                chatVariantId = chatVariantId
            )

            WssLogger.info("⚡️ Создание завершено. Повторный поиск конфига...")

            // Повторный запрос
            characterLLMConfig = characterLLMConfigService.get(
                id = chatVariantId,
                llmConfig = llmConfig
            )
        }

        // 4. Финальная проверка
        if (characterLLMConfig == null) {
            WssLogger.fail("Character Config not found (Access denied or Chat missing)")
            messageNotifier.notifyUserWithError(userId, "Chat not found")
            return null
        }

        WssLogger.info("Успешно получен конфиг")
        return characterLLMConfig.copy(conversationSummary = summary)
    }
}