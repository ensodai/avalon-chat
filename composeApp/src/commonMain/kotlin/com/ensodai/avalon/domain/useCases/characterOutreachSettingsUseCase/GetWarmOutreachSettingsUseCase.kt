package com.ensodai.avalon.domain.useCases.characterOutreachSettingsUseCase

import com.ensodai.shared.avalon.character.models.WarmOutreachSettingsDto
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Single
class GetWarmOutreachSettingsUseCase(
    private val characterRpcService: CharacterRpcService
) {
    suspend fun execute(characterId: Uuid): WarmOutreachSettingsDto {
        return characterRpcService.getWarmOutreachSettings(characterId)
    }
}
