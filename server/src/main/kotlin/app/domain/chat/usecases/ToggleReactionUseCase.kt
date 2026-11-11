package app.domain.chat.usecases

import app.data.db.tables.chat.MessageContainers
import app.data.db.tables.chat.MessageSlots
import app.data.db.tables.chatSessions.ChatSessions
import app.data.db.tables.chatSessions.ChatVariants
import com.ensodai.shared.avalon.chat.wss.ServerMessage
import app.service.chat.EmojiPalette
import app.service.chat.MessageNotifier
import app.utils.dbQuery
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

/**
 * UseCase для переключения реакции пользователя на сообщение.
 * Если присланная реакция совпадает с текущей — она удаляется (toggle).
 */
@Single
class ToggleReactionUseCase(
    private val messageNotifier: MessageNotifier,
) {

    /**
     * Основной метод для переключения реакции.
     * @return Возвращает новую реакцию (String) или null, если реакция была снята.
     */
    suspend fun toggleReaction(
        userId: Uuid,
        reaction: String?,
        characterId: Uuid,
        chatVariantId: Uuid,
        chatSessionId: Uuid,
        containerId: Uuid,
        slotId: Uuid,
        wssUserSessionId: Uuid
    ): String? {

        // 1. Выполняем все операции с БД внутри транзакции
        // Мы возвращаем результат в обертке, чтобы отличить "Ошибка валидации" (null)
        // от "Успешно установили null-реакцию" (ToggleResult(null))
        val result = dbQuery {
            val safeReaction = EmojiPalette.validateReaction(reaction)

            // Проверяем всю цепочку владения одним запросом.
            // Это эффективнее, чем 4 отдельных count()
            val isContextValid =
                (MessageContainers innerJoin MessageSlots innerJoin ChatVariants innerJoin ChatSessions)
                    .select(MessageContainers.id)
                    .where {
                        (MessageContainers.id eq containerId) and
                                (MessageSlots.id eq slotId) and
                                (ChatVariants.id eq chatVariantId) and
                                (ChatSessions.id eq chatSessionId) and
                                (ChatSessions.userId eq userId) and
                                (ChatSessions.characterId eq characterId) and
                                (MessageContainers.deletedAt.isNull()) and
                                (MessageSlots.deletedAt.isNull()) and
                                (ChatVariants.deletedAt.isNull()) and
                                (ChatSessions.deletedAt.isNull())
                    }
                    .count() > 0

            if (!isContextValid) {
                return@dbQuery null
            }

            // Логика переключения (Toggle)
            val currentReaction = MessageContainers
                .select(MessageContainers.userReaction)
                .where { MessageContainers.id eq containerId }
                .singleOrNull()
                ?.get(MessageContainers.userReaction)

            val nextReaction = if (currentReaction == safeReaction) null else safeReaction

            // Обновляем БД
            MessageContainers.update({ MessageContainers.id eq containerId }) {
                it[userReaction] = nextReaction
            }

            ToggleResult(nextReaction)
        }

        // 2. Если транзакция прошла успешно (результат не null) — отправляем уведомление
        if (result != null) {
            val newReactionValue = result.value

            val socketMessage = ServerMessage.SendUserReaction(
                messageContainerId = containerId,
                reaction = newReactionValue
            )

            // Теперь это suspend вызов вне транзакции
            messageNotifier.notifyUserWithMessage(
                userId = userId,
                message = socketMessage,
                excludeSessionId = wssUserSessionId
            )

            return newReactionValue
        }

        return null
    }

    /**
     * Вспомогательный класс-обертка для возврата результата из транзакции.
     */
    private data class ToggleResult(val value: String?)
}