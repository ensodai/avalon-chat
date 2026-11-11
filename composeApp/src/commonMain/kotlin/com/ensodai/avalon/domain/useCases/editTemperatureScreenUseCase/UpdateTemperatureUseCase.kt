package com.ensodai.avalon.domain.useCases.editTemperatureScreenUseCase

import com.ensodai.shared.avalon.character.models.UpdateTemperatureRequest
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import com.ensodai.avalon.domain.repository.local.LocalUpdateCharacterRepository
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class UpdateTemperatureUseCase(
    private val characterRpcService: CharacterRpcService,
    private val localUpdateCharacterRepository: LocalUpdateCharacterRepository
) {

    suspend fun update(
        characterId: Uuid,
        temperature: Float
    ): Result<Unit> = runCatching {
        characterRpcService.updateTemperature(
            UpdateTemperatureRequest(
                characterId = characterId,
                temperature = temperature
            )
        )
    }.onSuccess {
        localUpdateCharacterRepository.updateTemperature(
            characterId = characterId,
            temperature = temperature
        )
    }
}
