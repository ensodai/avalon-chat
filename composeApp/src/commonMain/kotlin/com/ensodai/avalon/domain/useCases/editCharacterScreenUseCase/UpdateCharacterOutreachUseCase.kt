package com.ensodai.avalon.domain.useCases.editCharacterScreenUseCase

import com.ensodai.shared.avalon.character.models.UpdateCharacterOutreachRequest
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import com.ensodai.avalon.domain.repository.local.LocalUpdateCharacterRepository
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class UpdateCharacterOutreachUseCase(
    private val characterRpcService: CharacterRpcService,
    private val localUpdateCharacterRepository: LocalUpdateCharacterRepository
) {

    suspend fun update(
        characterId: Uuid,
        isOutreachEnabled: Boolean
    ): Result<Unit> = runCatching {
        characterRpcService.updateOutreach(
            UpdateCharacterOutreachRequest(
                characterId = characterId,
                isOutreachEnabled = isOutreachEnabled
            )
        )
    }.onSuccess {
        localUpdateCharacterRepository.updateOutreach(
            characterId = characterId,
            isOutreachEnabled = isOutreachEnabled
        )
    }
}
