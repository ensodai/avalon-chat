package app.data.repository

import app.data.db.tables.aiCharacter.AICharacters
import app.data.db.tables.aiCharacter.AICharacters.characterName
import app.data.db.tables.aiCharacter.AICharacters.shortDescription
import app.data.db.tables.aiCharacter.AICharacters.temperature
import app.data.db.tables.aiCharacter.AICharacters.visibility
import app.data.db.tables.aiCharacter.CharacterWelcomeCard
import app.data.db.tables.images.CharacterAvatars
import app.data.db.tables.images.CharacterImages
import app.data.db.tables.images.WelcomeCardImages
import com.ensodai.shared.avalon.character.models.*
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import app.domain.utils.ErrorType
import app.domain.utils.ServiceResult
import app.utils.dbQuery
import app.utils.toServerUrl
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.statements.UpdateStatement
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import java.io.File
import kotlin.time.Clock
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single

@Single
class CharacterUpdateRepository {

    suspend fun updateName(userId: Uuid, charId: Uuid, name: String): ServiceResult<Unit> {
        return updateCharacter(userId, charId) {
            it[characterName] = name
        }
    }

    suspend fun updateDescription(userId: Uuid, charId: Uuid, desc: String): ServiceResult<Unit> {
        return updateCharacter(userId, charId) {
            it[shortDescription] = desc
        }
    }

    suspend fun updateFullDescription(
        userId: Uuid,
        characterId: Uuid,
        fullDescription: String,
        isPublicFullDescription: Boolean
    ): ServiceResult<Unit> {
        return updateCharacter(userId, characterId) {
            it[AICharacters.fullDescription] = fullDescription
            it[AICharacters.isPublicFullDescription] = isPublicFullDescription
        }
    }

    suspend fun updateVisibility(userId: Uuid, charId: Uuid, newVisibility: CharacterVisibility): ServiceResult<Unit> {
        return updateCharacter(userId, charId) {
            it[visibility] = newVisibility
        }
    }

    suspend fun updateTemperature(userId: Uuid, charId: Uuid, temp: Float): ServiceResult<Unit> {
        return updateCharacter(userId, charId) {
            it[temperature] = temp
        }
    }

    suspend fun updateFirstMessage(userId: Uuid, charId: Uuid, message: String?): ServiceResult<Unit> {
        return updateCharacter(userId, charId) {
            it[AICharacters.firstMessage] = message
        }
    }

    suspend fun updateOutreach(userId: Uuid, charId: Uuid, isOutreachEnabled: Boolean): ServiceResult<Unit> {
        return updateCharacter(userId, charId) {
            it[AICharacters.isOutreachEnabled] = isOutreachEnabled
        }
    }

    suspend fun updateVoice(userId: Uuid, charId: Uuid, voice: VoiceModel): ServiceResult<Unit> {
        return updateCharacter(userId, charId) {
            it[AICharacters.voice] = voice
        }
    }

    suspend fun updateInteractionFormat(userId: Uuid, charId: Uuid, format: InteractionFormat): ServiceResult<Unit> {
        return updateCharacter(userId, charId) {
            it[AICharacters.interactionFormat] = format
        }
    }

    suspend fun updateImages(
        userId: Uuid,
        charId: Uuid,
        newAvatar: app.service.ImageSaveResult?,
        newGalleryImages: List<app.service.ImageSaveResult>,
        deletedImagesIds: List<Uuid>,
        newAvatarId: Uuid?,
        galleryImagesSpecs: List<com.ensodai.shared.avalon.character.models.GalleryImageSpec>? = null,
        newImagesWithSpecs: List<NewImageWithSpec>? = null
    ): ServiceResult<UpdateImagesResponse> = dbQuery {

        val now = Clock.System.now()

        // --- 1. Проверка прав ---
        val charRow = AICharacters
            .select(AICharacters.owner, AICharacters.deletedAt)
            .where { AICharacters.id eq charId }
            .singleOrNull()

        if (charRow == null) return@dbQuery ServiceResult.Error(ErrorType.NOT_FOUND, "Character not found")
        if (charRow[AICharacters.deletedAt] != null) return@dbQuery ServiceResult.Error(
            ErrorType.NOT_FOUND,
            "Character is deleted"
        )
        if (charRow[AICharacters.owner].value != userId) return@dbQuery ServiceResult.Error(
            ErrorType.FORBIDDEN,
            "Not owner"
        )

        var contentChanged = false

        // --- 2. Получаем информацию о ТЕКУЩЕМ (старом) аватаре ---
        val currentAvatarRow = CharacterAvatars
            .select(CharacterAvatars.id, CharacterAvatars.imagePath, CharacterAvatars.thumbHash)
            .where {
                (CharacterAvatars.characterPublicId eq charId) and
                        (CharacterAvatars.deletedAt.isNull())
            }
            .singleOrNull()

        val currentAvatarId = currentAvatarRow?.get(CharacterAvatars.id)?.value
        val currentAvatarPath = currentAvatarRow?.get(CharacterAvatars.imagePath)
        val currentAvatarThumbHash = currentAvatarRow?.get(CharacterAvatars.thumbHash)

        // --- 3. Подготовка списков на удаление ---
        val galleryIdsToDelete = deletedImagesIds.filter { id ->
            id != newAvatarId && id != currentAvatarId
        }

        val isCurrentAvatarExplicitlyDeleted = currentAvatarId != null && deletedImagesIds.contains(currentAvatarId)
        val isSettingNewAvatar = newAvatar != null || newAvatarId != null


        // --- 4. Обработка ТЕКУЩЕГО (старого) аватара ---
        if (currentAvatarRow != null) {
            if (isSettingNewAvatar || isCurrentAvatarExplicitlyDeleted) {
                CharacterAvatars.update({ CharacterAvatars.id eq currentAvatarId!! }) {
                    it[deletedAt] = now
                }
                contentChanged = true

                if (isCurrentAvatarExplicitlyDeleted) {
                    try {
                        File(currentAvatarPath!!).delete()
                    } catch (e: Exception) {
                    }
                } else if (isSettingNewAvatar) {
                    CharacterImages.insert {
                        it[characterPublicId] = charId
                        it[imagePath] = currentAvatarPath!!
                        it[thumbHash] = currentAvatarThumbHash
                    }
                }
            }
        }

        // --- 5. Удаление картинок ГАЛЕРЕИ (обычный delete) ---
        if (galleryIdsToDelete.isNotEmpty()) {
            val pathsToDelete = CharacterImages
                .select(CharacterImages.imagePath)
                .where {
                    (CharacterImages.id inList galleryIdsToDelete) and
                            (CharacterImages.characterPublicId eq charId) and
                            (CharacterImages.deletedAt.isNull())
                }
                .map { it[CharacterImages.imagePath] }

            val count = CharacterImages.update({
                (CharacterImages.id inList galleryIdsToDelete) and (CharacterImages.characterPublicId eq charId)
            }) {
                it[deletedAt] = now
            }

            if (count > 0) {
                contentChanged = true
                pathsToDelete.forEach { path ->
                    try {
                        File(path).delete()
                    } catch (e: Exception) {
                    }
                }
            }
        }

        // --- 6. Установка НОВОГО аватара ---
        if (newAvatar != null) {
            CharacterAvatars.insert {
                it[characterPublicId] = charId
                it[imagePath] = newAvatar.path
                it[thumbHash] = newAvatar.thumbHash
            }
            contentChanged = true
        } else if (newAvatarId != null) {
            val galleryImageRow = CharacterImages
                .select(CharacterImages.imagePath, CharacterImages.thumbHash)
                .where {
                    (CharacterImages.id eq newAvatarId) and
                            (CharacterImages.characterPublicId eq charId) and
                            (CharacterImages.deletedAt.isNull())
                }
                .singleOrNull()

            if (galleryImageRow != null) {
                val path = galleryImageRow[CharacterImages.imagePath]
                val thumb = galleryImageRow[CharacterImages.thumbHash]

                CharacterAvatars.insert {
                    it[characterPublicId] = charId
                    it[imagePath] = path
                    it[thumbHash] = thumb
                }
                CharacterImages.update({ CharacterImages.id eq newAvatarId }) {
                    it[deletedAt] = now
                }
                contentChanged = true
            }
        }

        // --- 7. Обновление спецификаций существующих изображений ---
        galleryImagesSpecs?.forEach { spec ->
            if (spec.imageId != null) {
                CharacterImages.update({
                    (CharacterImages.id eq spec.imageId) and
                            (CharacterImages.characterPublicId eq charId)
                }) {
                    it[isPrivate] = spec.isPrivate
                    it[energyCost] = spec.energyCost
                    it[requiredLevel] = spec.requiredLevel
                }
                contentChanged = true
            }
        }

        // --- 7.1 Добавление новых картинок с детальными спецификациями ---
        if (newImagesWithSpecs != null && newImagesWithSpecs.isNotEmpty()) {
            newImagesWithSpecs.forEach { spec ->
                CharacterImages.insert {
                    it[characterPublicId] = charId
                    it[imagePath] = spec.saveResult.path
                    it[thumbHash] = spec.saveResult.thumbHash
                    it[isPrivate] = spec.isPrivate
                    it[energyCost] = spec.energyCost
                    it[requiredLevel] = spec.requiredLevel
                }
            }
            contentChanged = true
        }

        // --- 7.2 Добавление новых картинок в галерею (legacy) ---
        if (newGalleryImages.isNotEmpty()) {
            CharacterImages.batchInsert(newGalleryImages) { result ->
                this[CharacterImages.characterPublicId] = charId
                this[CharacterImages.imagePath] = result.path
                this[CharacterImages.thumbHash] = result.thumbHash
                this[CharacterImages.isPrivate] = false
                this[CharacterImages.energyCost] = 0
                this[CharacterImages.requiredLevel] = null
            }
            contentChanged = true
        }

        // --- 8. Обновление timestamp персонажа ---
        if (contentChanged) {
            AICharacters.update({ AICharacters.id eq charId }) {
                it[updatedAt] = now
            }
        }

        // --- 9. Сборка ответа ---
        val finalAvatar = CharacterAvatars
            .select(CharacterAvatars.id, CharacterAvatars.imagePath, CharacterAvatars.thumbHash)
            .where { (CharacterAvatars.characterPublicId eq charId) and (CharacterAvatars.deletedAt.isNull()) }
            .singleOrNull()
            ?.let {
                CharacterAvatarResponse(
                    id = it[CharacterAvatars.id].value,
                    imageUrl = it[CharacterAvatars.imagePath].toServerUrl(),
                    thumbHash = it[CharacterAvatars.thumbHash]
                )
            }

        val finalGallery = CharacterImages
            .select(
                CharacterImages.id,
                CharacterImages.imagePath,
                CharacterImages.thumbHash,
                CharacterImages.isPrivate,
                CharacterImages.energyCost,
                CharacterImages.requiredLevel
            )
            .where { (CharacterImages.characterPublicId eq charId) and (CharacterImages.deletedAt.isNull()) }
            .orderBy(CharacterImages.createdAt)
            .map {
                CharacterImageResponse(
                    id = it[CharacterImages.id].value,
                    imageUrl = it[CharacterImages.imagePath].toServerUrl(),
                    thumbHash = it[CharacterImages.thumbHash],
                    isPrivate = it[CharacterImages.isPrivate],
                    energyCost = it[CharacterImages.energyCost],
                    requiredLevel = it[CharacterImages.requiredLevel],
                    isUnlocked = true
                )
            }

        ServiceResult.Success(UpdateImagesResponse(finalAvatar, finalGallery))
    }

    suspend fun updateWelcomeCard(
        userId: Uuid,
        characterId: Uuid,
        config: UpdateWelcomeCardRequest,
        newImagePaths: List<app.service.ImageSaveResult>
    ): ServiceResult<UpdateWelcomeCardImagesResponse> = dbQuery {

        val now = Clock.System.now()

        // --- 1. Проверка прав доступа ---
        val charRow = AICharacters
            .select(AICharacters.owner, AICharacters.deletedAt)
            .where { AICharacters.id eq characterId }
            .singleOrNull()

        if (charRow == null) return@dbQuery ServiceResult.Error(ErrorType.NOT_FOUND, "Character not found")
        if (charRow[AICharacters.deletedAt] != null) return@dbQuery ServiceResult.Error(
            ErrorType.NOT_FOUND,
            "Character is deleted"
        )
        if (charRow[AICharacters.owner].value != userId) return@dbQuery ServiceResult.Error(
            ErrorType.FORBIDDEN,
            "Not owner"
        )

        // --- 2. Обновление текстовых настроек ---
        val existingCardStats = CharacterWelcomeCard
            .select(CharacterWelcomeCard.id)
            .where { CharacterWelcomeCard.character eq characterId }
            .count()

        if (existingCardStats > 0) {
            CharacterWelcomeCard.update({ CharacterWelcomeCard.character eq characterId }) {
                it[CharacterWelcomeCard.welcomeCardDescription] = config.description
                it[CharacterWelcomeCard.cardColorStart] = config.colorStart
                it[CharacterWelcomeCard.cardColorEnd] = config.colorEnd
                it[CharacterWelcomeCard.cardColorLight] = config.colorLight
                it[CharacterWelcomeCard.isEnableLight] = config.isLightEnabled
            }
        } else {
            CharacterWelcomeCard.insert {
                it[character] = characterId
                it[CharacterWelcomeCard.welcomeCardDescription] = config.description
                it[CharacterWelcomeCard.cardColorStart] = config.colorStart
                it[CharacterWelcomeCard.cardColorEnd] = config.colorEnd
                it[CharacterWelcomeCard.cardColorLight] = config.colorLight
                it[CharacterWelcomeCard.isEnableLight] = config.isLightEnabled
            }
        }

        // --- 3. Обработка удаляемых картинок ---
        val deletedIds = config.deletedImagesIds ?: emptyList()
        if (deletedIds.isNotEmpty()) {
            val pathsToDelete = WelcomeCardImages
                .select(WelcomeCardImages.imagePath)
                .where {
                    (WelcomeCardImages.id inList deletedIds) and
                            (WelcomeCardImages.characterPublicId eq characterId) and
                            (WelcomeCardImages.deletedAt.isNull())
                }
                .map { it[WelcomeCardImages.imagePath] }

            val updatedCount = WelcomeCardImages.update({
                (WelcomeCardImages.id inList deletedIds) and
                        (WelcomeCardImages.characterPublicId eq characterId)
            }) {
                it[deletedAt] = now
            }

            if (updatedCount > 0) {
                pathsToDelete.forEach { path ->
                    try {
                        File(path).delete()
                    } catch (e: Exception) {
                    }
                }
            }
        }

        // --- 4. Вставка новых картинок ---
        if (newImagePaths.isNotEmpty()) {
            WelcomeCardImages.batchInsert(newImagePaths) { result ->
                this[WelcomeCardImages.characterPublicId] = characterId
                this[WelcomeCardImages.imagePath] = result.path
                this[WelcomeCardImages.thumbHash] = result.thumbHash
            }
        }

        // --- 5. Обновляем дату изменения самого персонажа ---
        AICharacters.update({ AICharacters.id eq characterId }) {
            it[updatedAt] = now
        }

        // --- 6. Формирование ответа ---
        val activeImages = WelcomeCardImages
            .select(WelcomeCardImages.id, WelcomeCardImages.imagePath, WelcomeCardImages.thumbHash)
            .where {
                (WelcomeCardImages.characterPublicId eq characterId) and
                        (WelcomeCardImages.deletedAt.isNull())
            }
            .orderBy(WelcomeCardImages.createdAt)
            .map {
                WelcomeCardImageResponse(
                    id = it[WelcomeCardImages.id].value,
                    imageUrl = it[WelcomeCardImages.imagePath].toServerUrl()
                )
            }

        ServiceResult.Success(
            UpdateWelcomeCardImagesResponse(
                welcomeCardImages = activeImages
            )
        )
    }

    private suspend fun updateCharacter(
        userId: Uuid,
        charId: Uuid,
        updateAction: (UpdateStatement) -> Unit
    ): ServiceResult<Unit> = dbQuery {

        val row = AICharacters
            .select(AICharacters.owner, AICharacters.deletedAt)
            .where { AICharacters.id eq charId }
            .singleOrNull()

        if (row == null) {
            return@dbQuery ServiceResult.Error(ErrorType.NOT_FOUND, "Character not found")
        }

        if (row[AICharacters.deletedAt] != null) {
            return@dbQuery ServiceResult.Error(ErrorType.NOT_FOUND, "Character is deleted")
        }

        if (row[AICharacters.owner].value != userId) {
            return@dbQuery ServiceResult.Error(ErrorType.FORBIDDEN, "You are not the owner of this character")
        }

        AICharacters.update({ AICharacters.id eq charId }) {
            updateAction(it)
        }

        ServiceResult.Success(Unit)
    }
}
