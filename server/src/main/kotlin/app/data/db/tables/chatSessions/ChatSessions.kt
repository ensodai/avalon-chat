package app.data.db.tables.chatSessions

import app.core.BaseUuidEntity
import app.core.BaseUuidEntityClass
import app.core.BaseUuidTable
import app.data.db.tables.aiCharacter.AICharacter
import app.data.db.tables.aiCharacter.AICharacters
import app.data.db.tables.users.AppUser
import app.data.db.tables.users.AppUsers

import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import app.data.db.tables.chat.MessageContainers
import com.ensodai.shared.avalon.chat.models.MessageRole
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Instant
import kotlin.uuid.Uuid

object ChatSessions : BaseUuidTable("chat_sessions") {

    val userId = reference("user_id", AppUsers, onDelete = ReferenceOption.CASCADE)
    val characterId = reference("character_id", AICharacters, onDelete = ReferenceOption.CASCADE)
    val isArchived = bool("is_archived").default(false)
    val activeVariantId = reference("active_variant_id", ChatVariants, onDelete = ReferenceOption.SET_NULL).nullable()
    val lastMessageId = reference("last_message_id", MessageContainers, onDelete = ReferenceOption.SET_NULL).nullable()
    val lastMessagePreview = varchar("last_message_preview", 255).nullable()
    val lastMessageType = enumerationByName("last_message_type", 20, AvalonMessageContentType::class).nullable()
    val lastMessageAt = timestamp("last_message_at").nullable()
    val lastMessageRole = enumerationByName("last_message_role", 20, MessageRole::class).nullable()

    init {
        // миграция V33 скрипт индекс
        index("idx_chat_sessions_last_message_id", isUnique = false, lastMessageId)
        index("idx_chat_sessions_user_sync", isUnique = false, userId, updatedAt)
        // Ускоряет поиск "Есть ли у юзера чат с этим Персонажем?"
        // Часто используется при создании нового чата (проверка на дубли) или переходе с профиля персонажа.
        index("idx_chat_sessions_character", isUnique = false, userId, characterId)
    }
}

class ChatSession(id: EntityID<Uuid>) : BaseUuidEntity(id, ChatSessions) {
    companion object : BaseUuidEntityClass<ChatSession>(ChatSessions)

    var user by AppUser.Companion referencedOn ChatSessions.userId
    var aiCharacter by AICharacter referencedOn ChatSessions.characterId
    var isArchived by ChatSessions.isArchived
    var activeVariant by ChatVariant.Companion optionalReferencedOn ChatSessions.activeVariantId
    val variants by ChatVariant.Companion referrersOn ChatVariants.chatSessionId
}

data class ChatSessionInternalDto(
    val id: Uuid,
    val userId: Uuid,
    val characterId: Uuid,
    val isArchived: Boolean,
    val createdAt: Instant,
    val lastUpdatedAt: Instant,
    val activeVariantId: Uuid?,
    val variants: List<ChatVariantInternalDto>
)

// ВАЖНО: Вызывать, когда `user`, `aiCharacter` и `variants` загружены через .with()
fun ChatSession.toInternalDto(): ChatSessionInternalDto {
    return ChatSessionInternalDto(
        id = this.id.value,
        userId = this.user.id.value,
        characterId = this.aiCharacter.id.value,
        isArchived = this.isArchived,
        createdAt = this.createdAt,
        lastUpdatedAt = this.updatedAt,
        activeVariantId = this.activeVariant?.id?.value,
        variants = this.variants.map { it.toInternalDto() }
    )
}

fun ChatSession.toPreviewResponse(): com.ensodai.shared.avalon.chat.models.ChatSessionPreviewResponse {
    return com.ensodai.shared.avalon.chat.models.ChatSessionPreviewResponse(
        id = this.id.value,
        characterId = this.aiCharacter.id.value,
        isArchived = this.isArchived,
        activeVariantId = this.activeVariant?.id?.value,
        updatedAt = this.updatedAt,
        createdAt = this.createdAt,
        lastMessage = null // TODO: map last message if needed
    )
}
