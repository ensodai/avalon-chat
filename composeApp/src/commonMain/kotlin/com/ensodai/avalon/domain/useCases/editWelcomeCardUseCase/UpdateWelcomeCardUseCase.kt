package com.ensodai.avalon.domain.useCases.editWelcomeCardUseCase

import com.ensodai.shared.avalon.character.models.UpdateWelcomeCardRequest
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import com.ensodai.avalon.domain.repository.local.LocalUpdateCharacterRepository
import com.ensodai.avalon.presentation.addCharacterScreen.viewState.UploadedWelcomeCardImageRef
import com.ensodai.avalon.utils.AppLogger
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class UpdateWelcomeCardUseCase(
    private val characterRpcService: CharacterRpcService,
    private val localUpdateCharacterRepository: LocalUpdateCharacterRepository,
    private val logger: AppLogger
) {

    suspend fun update(
        characterId: Uuid,
        attachedImages: List<UploadedWelcomeCardImageRef>?,
        welcomeCardConfig: UpdateWelcomeCardConfig
    ): Result<Unit> = runCatching {
        val imageTokens = attachedImages?.map { it.uploadToken } ?: emptyList()
        logger.d(
            "UpdateWelcomeCard",
            "Updating welcome card with ${imageTokens.size} pre-uploaded images"
        )

        val response = characterRpcService.updateWelcomeCard(
            UpdateWelcomeCardRequest(
                characterId = characterId,
                description = welcomeCardConfig.welcomeCardDescription,
                colorStart = welcomeCardConfig.startColor,
                colorEnd = welcomeCardConfig.endColor,
                colorLight = welcomeCardConfig.colorLight,
                isLightEnabled = welcomeCardConfig.isEnableLight,
                deletedImagesIds = welcomeCardConfig.deletedImages,
                imageTokens = imageTokens
            )
        )
        
        val welcomeCardImages = response.welcomeCard?.welcomeCardImages ?: emptyList()

        logger.d("UpdateWelcomeCard", "Welcome card updated successfully")

        // ====================================================================
        // ШАГ 3: Сохраняем данные в локальную БД
        // ====================================================================

        localUpdateCharacterRepository.updateWelcomeCard(
            characterId = characterId,
            welcomeCardImages = welcomeCardImages,
            welcomeCardConfig = welcomeCardConfig
        ).getOrThrow()
    }.onFailure { error ->
        logger.e(
            "UpdateWelcomeCard",
            "Failed to update welcome card: ${error.stackTraceToString()}"
        )
    }
}
