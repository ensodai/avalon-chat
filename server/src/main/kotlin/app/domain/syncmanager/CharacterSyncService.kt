package app.domain.syncmanager

import app.data.db.tables.aiCharacter.AICharacters
import app.data.db.tables.aiCharacter.CharacterWelcomeCard
import app.data.db.tables.chatSessions.ChatSessions
import app.data.db.tables.images.CharacterAvatars
import app.data.db.tables.images.CharacterImages
import app.data.db.tables.images.WelcomeCardImages
import app.data.db.tables.users.AppUsers
import com.ensodai.shared.avalon.character.models.*
import com.ensodai.shared.avalon.character.models.CharacterVisibility as SharedCharacterVisibility
import app.utils.toServerUrl
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.select
import kotlin.time.Instant
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single

@Single
class CharacterSyncService {

    fun fetchChanges(
        userId: Uuid,
        since: Instant,
        forcedCharacterIds: List<Uuid>
    ): SyncEntityChanges<com.ensodai.shared.avalon.character.models.CharacterResponse> {

        // 1. Подзапрос: С какими персонажами у меня есть ЖИВЫЕ чаты?
        val userActiveChatsQuery = ChatSessions
            .select(ChatSessions.characterId)
            .where {
                (ChatSessions.userId eq userId) and
                        (ChatSessions.deletedAt.isNull())
            }

        // 2. Ищем ОБНОВЛЕННЫХ персонажей
        val characterRows = AICharacters
            .innerJoin(AppUsers)
            .join(CharacterWelcomeCard, JoinType.LEFT, AICharacters.id, CharacterWelcomeCard.character)
            .select(
                AICharacters.id,
                AICharacters.owner,
                AICharacters.characterName,
                AICharacters.shortDescription,
                AICharacters.fullDescription,
                AICharacters.firstMessage,
                AICharacters.updatedAt,
                AICharacters.deletedAt,
                AICharacters.isPublicFullDescription,
                AICharacters.visibility,
                AICharacters.temperature,
                AICharacters.isOutreachEnabled,

                AppUsers.nickName,

                CharacterWelcomeCard.id,
                CharacterWelcomeCard.welcomeCardDescription,
                CharacterWelcomeCard.cardColorStart,
                CharacterWelcomeCard.cardColorEnd,
                CharacterWelcomeCard.cardColorLight,
                CharacterWelcomeCard.isEnableLight
            )
            .where {
                // Условие А: Активный чат (для собеседников это вернет true, для владельца - false, т.к. сессия удалена)
                val isChatting = AICharacters.id inSubQuery userActiveChatsQuery

                // Условие Б: Я владелец И персонаж ЖИВ
                val isOwnerAndAlive = (AICharacters.owner eq userId) and (AICharacters.deletedAt.isNull())

                // Условие В: Форс
                val isForced = if (forcedCharacterIds.isNotEmpty()) {
                    AICharacters.id inList forcedCharacterIds
                } else Op.FALSE

                // Глобальный фильтр по времени
                val hasChanged = AICharacters.updatedAt greater since

                // === ГЛАВНОЕ ИЗМЕНЕНИЕ ===
                // Явное определение: Я владелец удаленного персонажа.
                val isOwnerDeleted = (AICharacters.owner eq userId) and (AICharacters.deletedAt.isNotNull())

                // Логика:
                // 1. Персонаж должен подходить под условия (Чат ИЛИ ЖивойВладелец ИЛИ Форс)
                // 2. Персонаж должен обновиться после 'since'
                // 3. И (ВАЖНО!) Это НЕ должен быть мой собственный удаленный персонаж.
                //    Если это мой удаленный персонаж -> он должен попасть ТОЛЬКО в deletedIds (блок 4).
                (isChatting or isOwnerAndAlive or isForced) and hasChanged and not(isOwnerDeleted)
            }
            .toList()

        val characterIds = characterRows.map { it[AICharacters.id] }

        // 3. ЗАГРУЗКА ЗАВИСИМОСТЕЙ (Аватарки и Картинки)
        val updated = if (characterIds.isNotEmpty()) {

            val avatarsMap = CharacterAvatars
                .select(CharacterAvatars.characterPublicId, CharacterAvatars.id, CharacterAvatars.imagePath)
                .where { (CharacterAvatars.characterPublicId inList characterIds) and (CharacterAvatars.deletedAt.isNull()) }
                .associateBy { it[CharacterAvatars.characterPublicId] }


            val imagesMap =
                CharacterImages.select(
                    CharacterImages.characterPublicId,
                    CharacterImages.id,
                    CharacterImages.imagePath
                )
                    .where { (CharacterImages.characterPublicId inList characterIds) and (CharacterImages.deletedAt.isNull()) }
                    .orderBy(CharacterImages.orderIndex).groupBy { it[CharacterImages.characterPublicId] }
            val welcomeCardImagesMap = WelcomeCardImages.select(
                WelcomeCardImages.characterPublicId,
                WelcomeCardImages.id,
                WelcomeCardImages.imagePath
            )
                .where { (WelcomeCardImages.characterPublicId inList characterIds) and (WelcomeCardImages.deletedAt.isNull()) }
                .orderBy(WelcomeCardImages.orderIndex).groupBy { it[WelcomeCardImages.characterPublicId] }


            characterRows.map { row ->
                val charId = row[AICharacters.id]
                val ownerId = row[AICharacters.owner].value
                val isDeleted = row[AICharacters.deletedAt] != null

                // Строгая проверка: если удален, контент не показываем никому
                val strictlyShowContent = !isDeleted

                val avatarResp = if (strictlyShowContent) avatarsMap[charId]?.let {
                    com.ensodai.shared.avalon.character.models.CharacterAvatarResponse(it[CharacterAvatars.id].value, it[CharacterAvatars.imagePath].toServerUrl())
                } else null

                val imagesList = if (strictlyShowContent) imagesMap[charId]?.map {
                    com.ensodai.shared.avalon.character.models.CharacterImageResponse(it[CharacterImages.id].value, it[CharacterImages.imagePath].toServerUrl())
                } ?: emptyList() else emptyList()

                // Welcome Card
                val welcomeCardResp = if (strictlyShowContent && row.getOrNull(CharacterWelcomeCard.id) != null) {
                    val wcImagesList = welcomeCardImagesMap[charId]?.map {
                        com.ensodai.shared.avalon.character.models.WelcomeCardImageResponse(
                            it[WelcomeCardImages.id].value,
                            it[WelcomeCardImages.imagePath].toServerUrl()
                        )
                    }
                    com.ensodai.shared.avalon.character.models.WelcomeCardResponse(
                        id = row[CharacterWelcomeCard.id].value,
                        welcomeCardDescription = row[CharacterWelcomeCard.welcomeCardDescription],
                        welcomeCardImages = wcImagesList,
                        startColor = row[CharacterWelcomeCard.cardColorStart],
                        endColor = row[CharacterWelcomeCard.cardColorEnd],
                        cardColorLight = row[CharacterWelcomeCard.cardColorLight],
                        isEnableLight = row[CharacterWelcomeCard.isEnableLight]
                    )
                } else null
                com.ensodai.shared.avalon.character.models.CharacterResponse(
                    id = charId.value,
                    ownerNick = row[AppUsers.nickName],
                    characterName = row[AICharacters.characterName],
                    shortDescription = row[AICharacters.shortDescription],
                    fullDescription = if (strictlyShowContent) row[AICharacters.fullDescription] else null,
                    isPublicFullDescription = row[AICharacters.isPublicFullDescription],
                    visibility = row[AICharacters.visibility],
                    avatar = avatarResp,
                    images = imagesList,
                    welcomeCard = welcomeCardResp,
                    firstMessage = if (strictlyShowContent) row[AICharacters.firstMessage] else null,
                    canEdit = ownerId == userId,
                    updatedAt = row[AICharacters.updatedAt],
                    temperature = row[AICharacters.temperature],
                    isDeleted = isDeleted,
                    isOutreachEnabled = row[AICharacters.isOutreachEnabled]
                )
            }
        } else {
            emptyList()
        }

        val isFirstSync = since.toLocalDateTime(TimeZone.UTC).year < 2000

        // 4. Ищем УДАЛЕННЫХ персонажей
        // Сюда попадают ТОЛЬКО те, кого клиент должен удалить из локальной базы.
        val deletedIds = if (isFirstSync) {
            emptyList()
        } else {
            AICharacters
                .select(AICharacters.id)
                .where {
                    // Условие: Я владелец И Персонаж удален
                    // Благодаря фильтру isOwnerDeleted.not() в запросе выше,
                    // ID попадет только сюда, а не в updated.
                    val isOwnerDeleted = (AICharacters.owner eq userId) and (AICharacters.deletedAt.isNotNull())

                    isOwnerDeleted and (AICharacters.updatedAt greater since)
                }
                .map { it[AICharacters.id].value }
        }

        return SyncEntityChanges(updated, deletedIds)
    }
}