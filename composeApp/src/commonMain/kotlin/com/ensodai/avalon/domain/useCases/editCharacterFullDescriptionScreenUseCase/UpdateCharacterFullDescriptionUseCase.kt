package com.ensodai.avalon.domain.useCases.editCharacterFullDescriptionScreenUseCase

import com.ensodai.shared.avalon.character.models.UpdateFullDescriptionRequest
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import com.ensodai.avalon.domain.repository.local.LocalUpdateCharacterRepository
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class UpdateCharacterFullDescriptionUseCase(
    private val characterRpcService: CharacterRpcService,
    private val localUpdateCharacterRepository: LocalUpdateCharacterRepository
) {

    suspend fun update(
        characterId: Uuid,
        fullDescription: String,
        isPublicFullDescription: Boolean
    ): Result<Unit> = runCatching {
        characterRpcService.updateFullDescription(
            UpdateFullDescriptionRequest(
                characterId = characterId,
                fullDescription = fullDescription,
                isPublicFullDescription = isPublicFullDescription
            )
        )
    }.onSuccess {
        localUpdateCharacterRepository.updateFullDescription(
            characterId = characterId,
            fullDescription = fullDescription,
            isPublicFullDescription = isPublicFullDescription
        )
    }
}