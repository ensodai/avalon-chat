package com.ensodai.avalon.domain.useCases.editCharacterScreenUseCase

import com.ensodai.shared.avalon.character.models.UpdateVoiceRequest
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import com.ensodai.avalon.domain.repository.local.LocalUpdateCharacterRepository
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class UpdateCharacterVoiceUseCase(
    private val characterRpcService: CharacterRpcService,
    private val localUpdateCharacterRepository: LocalUpdateCharacterRepository
) {

    suspend fun update(
        characterId: Uuid,
        voice: VoiceModel
    ): Result<Unit> = runCatching {
        characterRpcService.updateVoice(
            UpdateVoiceRequest(
                characterId = characterId,
                voice = voice
            )
        )
    }.onSuccess {
        localUpdateCharacterRepository.updateVoice(
            characterId = characterId,
            voice = voice
        )
    }
}
