package com.ensodai.avalon.domain.useCases.editCharacterAvatarScreenUseCase

import com.ensodai.shared.avalon.character.models.UpdateImagesRequest
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.domain.repository.local.LocalUpdateCharacterRepository
import com.ensodai.avalon.presentation.editCharacterAvatarScreen.viewState.AttachedCharacterImageUpload
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class UpdateCharacterImagesUseCase(
    private val characterRpcService: CharacterRpcService,
    private val localUpdateCharacterRepository: LocalUpdateCharacterRepository
) {

    suspend fun update(
        characterId: Uuid,
        attachedImages: List<AttachedCharacterImageUpload>,
        deletedImages: List<CharacterImageDTO>,
        avatar: CharacterImageDTO?,
        characterImages: List<CharacterImageDTO>
    ): Result<Unit> = runCatching {

        // 1. Извлекаем токен аватара, если он новый (из прикрепленных)
        val avatarToken = if (avatar != null) {
            attachedImages.firstOrNull { it.image == avatar }?.uploadToken
        } else null

        // 2. Извлекаем токены только публичных новых картинок (legacy-путь).
        // Приватные изображения отправляются ТОЛЬКО через galleryImages со спецификациями,
        // чтобы не допустить перезаписи флага isPrivate через legacy-путь на бэкенде.
        val galleryTokens = attachedImages
            .filter { it.image != avatar && !it.image.isPrivate }
            .map { it.uploadToken }

        val newAvatarId = if (avatarToken == null) avatar?.imagePublicId else null
        val deletedImagesIds = deletedImages.map { it.imagePublicId }

        // Собираем спецификации для приватных / уровневых картинок
        val gallerySpecs = attachedImages.filter { it.image != avatar }.map { attached ->
            com.ensodai.shared.avalon.character.models.GalleryImageSpec(
                imageToken = attached.uploadToken,
                imageId = null,
                isPrivate = attached.image.isPrivate,
                energyCost = attached.image.energyCost,
                requiredLevel = attached.image.requiredLevel
            )
        } + attachedImages.filter { it.image == avatar }.map { attached ->
            com.ensodai.shared.avalon.character.models.GalleryImageSpec(
                imageToken = attached.uploadToken,
                imageId = null,
                isPrivate = false,
                energyCost = 0,
                requiredLevel = null
            )
        } + characterImages.filter { it.imagePublicId != avatar?.imagePublicId && !deletedImagesIds.contains(it.imagePublicId) }.map { img ->
            com.ensodai.shared.avalon.character.models.GalleryImageSpec(
                imageToken = null,
                imageId = img.imagePublicId,
                isPrivate = img.isPrivate,
                energyCost = img.energyCost,
                requiredLevel = img.requiredLevel
            )
        }

        // 3. Отправляем всё в RPC
        val response = characterRpcService.updateCharacterImages(
            UpdateImagesRequest(
                characterId = characterId,
                newAvatarId = newAvatarId,
                deletedImagesIds = deletedImagesIds,
                imageTokens = galleryTokens.ifEmpty { null },
                avatarToken = avatarToken,
                galleryImages = gallerySpecs.ifEmpty { null }
            )
        )

        // 4. Обновляем локальную БД
        localUpdateCharacterRepository.updateImages(
            avatar = response.avatar,
            images = response.gallery,
            characterId = characterId
        )
    }
}
