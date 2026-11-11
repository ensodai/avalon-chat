package app.domain.chat.usecases

import app.data.db.tables.chat.MessageReadReceiptsTable
import app.data.db.tables.chatSessions.ChatSessions
import app.data.db.tables.chatSessions.ChatVariants
import app.data.db.tables.chat.MessageSlots
import app.data.db.tables.chat.MessageContainers
import com.ensodai.shared.avalon.chat.models.MessageRole
import app.service.chat.MessageNotifier
import app.utils.dbQuery
import app.utils.getUTCTimeNow
import com.ensodai.shared.avalon.chat.wss.ServerMessage
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.not
import org.jetbrains.exposed.v1.core.inSubQuery
import org.jetbrains.exposed.v1.core.innerJoin
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

private val log = KotlinLogging.logger {}

/**
 * # MarkMessagesAsReadUseCase (Прочтение сообщений во вьюпорте)
 *
 * Сценарий использования для пакетного или одиночного прочтения сообщений пользователем.
 * Помечает сообщения прочитанными, генерируя квитанции прочтения, и оповещает UI в реальном времени.
 */
@Factory
class MarkMessagesAsReadUseCase(
    private val messageNotifier: MessageNotifier
) {
    /**
     * Помечает указанные сообщения как прочитанные.
     *
     * @param userId Идентификатор пользователя, читающего сообщения
     * @param messageContainerIds Список идентификаторов контейнеров сообщений для пометки
     */
    suspend fun execute(userId: Uuid, messageContainerIds: List<Uuid>) {
        if (messageContainerIds.isEmpty()) return

        val now = getUTCTimeNow()
        val distinctIds = messageContainerIds.distinct()

        val newReadIds = dbQuery {
            // 1. Выбираем из базы квитанции, которые уже существуют для этого юзера и этих сообщений
            val alreadyRead = MessageReadReceiptsTable
                .select(MessageReadReceiptsTable.messageContainerId)
                .where { 
                    (MessageReadReceiptsTable.userId eq userId) and 
                    (MessageReadReceiptsTable.messageContainerId inList distinctIds) 
                }
                .map { it[MessageReadReceiptsTable.messageContainerId].value }
                .toSet()

            // 2. Оставляем только те ID, которые действительно новые и не прочитаны
            val toInsert = distinctIds.filter { it !in alreadyRead }

            if (toInsert.isNotEmpty()) {
                // 3. Выполняем пакетную вставку квитанций с игнорированием дубликатов (ON CONFLICT DO NOTHING)
                MessageReadReceiptsTable.batchInsert(toInsert, ignore = true) { containerId ->
                    this[MessageReadReceiptsTable.id] = Uuid.random()
                    this[MessageReadReceiptsTable.messageContainerId] = containerId
                    this[MessageReadReceiptsTable.userId] = userId
                    this[MessageReadReceiptsTable.readAt] = now
                    this[MessageReadReceiptsTable.createdAt] = now
                    this[MessageReadReceiptsTable.updatedAt] = now
                }
                log.info { "MarkMessagesAsReadUseCase: Пользователь $userId пометил прочитанными ${toInsert.size} сообщений." }

                // --- Пересчет unread_count для затронутых вариантов чата ---
                val variantIds = (MessageContainers innerJoin MessageSlots)
                    .select(MessageSlots.chatVariantId)
                    .where { MessageContainers.id inList toInsert }
                    .map { it[MessageSlots.chatVariantId].value }
                    .distinct()

                val readReceiptsQuery = MessageReadReceiptsTable
                    .select(MessageReadReceiptsTable.messageContainerId)
                    .where { MessageReadReceiptsTable.userId eq userId }

                for (variantId in variantIds) {
                    val unreadQuery = (MessageContainers innerJoin MessageSlots)
                        .select(MessageContainers.id)
                        .where {
                            (MessageSlots.chatVariantId eq variantId) and
                            (MessageContainers.role eq MessageRole.MODEL) and
                            not(MessageContainers.id inSubQuery readReceiptsQuery)
                        }
                    val count = unreadQuery.count().toInt()

                    ChatVariants.update({ ChatVariants.id eq variantId }) {
                        it[unreadCount] = count
                        it[updatedAt] = now
                    }
                }

                toInsert
            } else {
                emptyList()
            }
        }

        // 4. Оповещаем UI в реальном времени, если были прочитаны новые сообщения
        if (newReadIds.isNotEmpty()) {
            // А. Отправляем события MessagesRead в вебсокет-флоу событий чата
            messageNotifier.notifyUserWithMessage(
                userId = userId,
                message = ServerMessage.MessagesRead(
                    messageContainerIds = newReadIds,
                    userId = userId,
                    timestamp = now
                )
            )
        }
    }
}
