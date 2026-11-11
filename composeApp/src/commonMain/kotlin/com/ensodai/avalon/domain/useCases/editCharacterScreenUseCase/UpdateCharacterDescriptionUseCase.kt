package com.ensodai.avalon.domain.useCases.editCharacterScreenUseCase

import com.ensodai.shared.avalon.character.models.UpdateDescriptionRequest
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import com.ensodai.avalon.domain.repository.local.LocalUpdateCharacterRepository
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class UpdateCharacterDescriptionUseCase(
    private val characterRpcService: CharacterRpcService,
    private val localUpdateCharacterRepository: LocalUpdateCharacterRepository
) {

    suspend fun update(
        characterId: Uuid,
        description: String
    ): Result<Unit> = runCatching {
        characterRpcService.updateDescription(
            UpdateDescriptionRequest(
                characterId = characterId,
                description = description
            )
        )
    }.onSuccess {
        localUpdateCharacterRepository.updateDescription(
            characterId = characterId,
            description = description
        )
    }
}
