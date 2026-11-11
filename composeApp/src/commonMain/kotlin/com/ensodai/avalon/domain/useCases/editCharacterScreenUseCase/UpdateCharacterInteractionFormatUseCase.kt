package com.ensodai.avalon.domain.useCases.editCharacterScreenUseCase

import com.ensodai.shared.avalon.character.models.UpdateInteractionFormatRequest
import com.ensodai.shared.avalon.character.models.InteractionFormat
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import com.ensodai.avalon.domain.repository.local.LocalUpdateCharacterRepository
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class UpdateCharacterInteractionFormatUseCase(
    private val characterRpcService: CharacterRpcService,
    private val localUpdateCharacterRepository: LocalUpdateCharacterRepository
) {

    suspend fun update(
        characterId: Uuid,
        interactionFormat: InteractionFormat
    ): Result<Unit> = runCatching {
        characterRpcService.updateInteractionFormat(
            UpdateInteractionFormatRequest(
                characterId = characterId,
                interactionFormat = interactionFormat
            )
        )
    }.onSuccess {
        localUpdateCharacterRepository.updateInteractionFormat(
            characterId = characterId,
            interactionFormat = interactionFormat
        )
    }
}
