package com.ensodai.avalon.domain.useCases.editCharacterScreenUseCase

import com.ensodai.shared.avalon.character.models.UpdateFirstMessageRequest
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import com.ensodai.avalon.domain.repository.local.LocalUpdateCharacterRepository
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class UpdateFirstMessageUseCase(
    private val characterRpcService: CharacterRpcService,
    private val localUpdateCharacterRepository: LocalUpdateCharacterRepository
) {

    suspend fun update(
        characterId: Uuid,
        firstMessage: String
    ): Result<Unit> = runCatching {
        characterRpcService.updateFirstMessage(
            UpdateFirstMessageRequest(
                characterId = characterId,
                message = firstMessage
            )
        )
    }.onSuccess {
        localUpdateCharacterRepository.updateFirstMessage(
            characterId = characterId,
            message = firstMessage
        )
    }
}