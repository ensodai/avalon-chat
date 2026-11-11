package com.ensodai.avalon.domain.useCases.editCharacterScreenUseCase

import com.ensodai.shared.avalon.character.models.UpdateNameRequest
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import com.ensodai.avalon.domain.repository.local.LocalUpdateCharacterRepository
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class UpdateCharacterNameUseCase(
    private val characterRpcService: CharacterRpcService,
    private val localUpdateCharacterRepository: LocalUpdateCharacterRepository
) {

    suspend fun update(
        characterId: Uuid,
        name: String
    ): Result<Unit> = runCatching {
        characterRpcService.updateName(
            UpdateNameRequest(
                characterId = characterId,
                newName = name
            )
        )
    }.onSuccess {
        localUpdateCharacterRepository.updateName(
            characterId = characterId,
            newName = name
        )
    }
}
