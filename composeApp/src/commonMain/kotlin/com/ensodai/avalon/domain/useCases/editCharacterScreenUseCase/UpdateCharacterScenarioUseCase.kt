package com.ensodai.avalon.domain.useCases.editCharacterScreenUseCase

import com.ensodai.shared.avalon.character.models.UpdateScenarioRequest
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import com.ensodai.avalon.domain.repository.local.LocalUpdateCharacterRepository
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class UpdateCharacterScenarioUseCase(
    private val characterRpcService: CharacterRpcService,
    private val localUpdateCharacterRepository: LocalUpdateCharacterRepository
) {

    suspend fun update(
        characterId: Uuid,
        scenario: String?
    ): Result<Unit> = runCatching {
        characterRpcService.updateScenario(
            UpdateScenarioRequest(
                characterId = characterId,
                scenario = scenario
            )
        )
    }.onSuccess {
        localUpdateCharacterRepository.updateScenario(characterId, scenario)
    }
}
