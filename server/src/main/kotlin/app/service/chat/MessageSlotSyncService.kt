package app.service.chat

import app.data.db.tables.chatSessions.ChatSessions
import app.data.db.tables.chatSessions.ChatVariants
import app.data.db.tables.chat.MessageSlots
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.select
import kotlin.time.Instant
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single

@Single
class MessageSlotSyncService {

    fun fetchDeletedSlots(
        userId: Uuid,
        since: Instant
    ): List<Uuid> {
        // 1. Проверка на первый запуск (EPOCH).
        // Если клиент пришел с датой 1970 года, значит у него чистая база.
        // Ему не нужно знать, что мы удаляли год назад.
        val isFirstLaunch = since.toLocalDateTime(TimeZone.UTC).year == 1970
        if (isFirstLaunch) {
            return emptyList()
        }

        // 2. Запрос в БД
        return MessageSlots
            .innerJoin(ChatVariants) // Джойним Варианты
            .innerJoin(ChatSessions) // Джойним Сессии
            .select(MessageSlots.id)
            .where {
                // Только слоты этого юзера
                (ChatSessions.userId eq userId) and

                        // Слот должен быть помечен как удаленный
                        (MessageSlots.deletedAt.isNotNull()) and

                        // Удаление произошло ПОСЛЕ последнего синка
                        (MessageSlots.deletedAt greater since) and

                        // ВАЖНО: Родительский Вариант должен быть ЖИВ
                        (ChatVariants.deletedAt.isNull()) and

                        // ВАЖНО: Родительская Сессия должна быть ЖИВА
                        (ChatSessions.deletedAt.isNull())
            }
            .map { it[MessageSlots.id].value }
    }
}