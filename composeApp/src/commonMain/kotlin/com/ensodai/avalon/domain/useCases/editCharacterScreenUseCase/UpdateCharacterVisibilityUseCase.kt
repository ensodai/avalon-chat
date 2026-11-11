package com.ensodai.avalon.domain.useCases.editCharacterScreenUseCase

import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.shared.avalon.character.models.UpdatePrivacyRequest
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import com.ensodai.avalon.domain.repository.local.LocalUpdateCharacterRepository
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class UpdateCharacterVisibilityUseCase(
    private val characterRpcService: CharacterRpcService,
    private val localUpdateCharacterRepository: LocalUpdateCharacterRepository
) {

    suspend fun update(
        characterId: Uuid,
        visibility: CharacterVisibility
    ): Result<Unit> = runCatching {
        characterRpcService.updatePrivacy(
            UpdatePrivacyRequest(
                characterId = characterId,
                characterVisibility = visibility
            )
        )
    }.onSuccess {
        localUpdateCharacterRepository.updatePrivacy(
            characterId = characterId,
            characterVisibility = visibility
        )
    }
}
