package com.ensodai.avalon.domain.useCases.editCharacterScreenUseCase

import com.ensodai.avalon.data.db.models.character.CharacterEditData
import com.ensodai.avalon.domain.repository.local.LocalCharacterRepository
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import com.ensodai.avalon.domain.useCases.commonUseCase.CharacterSaver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class GetCharacterEditDataUseCase(
    private val localCharacterRepository: LocalCharacterRepository,
    private val characterRpcService: CharacterRpcService,
    private val characterSaver: CharacterSaver
) {

    fun get(characterId: Uuid): Flow<CharacterEditData?> {
        return channelFlow {
            val producerScope = this

            val localJob = producerScope.launch {
                localCharacterRepository.getCharacterEditData(characterId)
                    .collect { tuple ->
                        send(tuple)
                    }
            }

            producerScope.launch {
                runCatching {
                    characterRpcService.getCharacter(characterPublicId = characterId)
                }.fold(
                    onSuccess = { character ->
                        characterSaver.save(character)
                    },
                    onFailure = {
                        it.printStackTrace()
                    }
                )
            }

            localJob.join()
        }
    }
}