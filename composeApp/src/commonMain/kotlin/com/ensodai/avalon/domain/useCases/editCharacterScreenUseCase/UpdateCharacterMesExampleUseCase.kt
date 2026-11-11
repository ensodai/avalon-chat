package com.ensodai.avalon.domain.useCases.editCharacterScreenUseCase

import com.ensodai.shared.avalon.character.models.UpdateMesExampleRequest
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import com.ensodai.avalon.domain.repository.local.LocalUpdateCharacterRepository
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class UpdateCharacterMesExampleUseCase(
    private val characterRpcService: CharacterRpcService,
    private val localUpdateCharacterRepository: LocalUpdateCharacterRepository
) {

    suspend fun update(
        characterId: Uuid,
        mesExample: String?
    ): Result<Unit> = runCatching {
        characterRpcService.updateMesExample(
            UpdateMesExampleRequest(
                characterId = characterId,
                mesExample = mesExample
            )
        )
    }.onSuccess {
        localUpdateCharacterRepository.updateMesExample(characterId, mesExample)
    }
}
