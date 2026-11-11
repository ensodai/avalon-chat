package com.ensodai.avalon.domain.useCases.characterEventsScreenUseCase

import com.ensodai.shared.avalon.character.models.CharacterEventDto
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Single
class GetCharacterEventsUseCase(
    private val characterRpcService: CharacterRpcService
) {
    suspend fun execute(characterId: Uuid): List<CharacterEventDto> {
        return characterRpcService.getCharacterEvents(characterId)
    }
}
