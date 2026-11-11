package com.ensodai.avalon.domain.useCases.characterEventsScreenUseCase

import com.ensodai.shared.avalon.character.models.CharacterEventDto
import com.ensodai.shared.avalon.character.models.UpdateCharacterEventsRequest
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Single
class UpdateCharacterEventsUseCase(
    private val characterRpcService: CharacterRpcService
) {
    suspend fun execute(characterId: Uuid, events: List<CharacterEventDto>) {
        characterRpcService.updateCharacterEvents(
            UpdateCharacterEventsRequest(
                characterId = characterId,
                events = events
            )
        )
    }
}
