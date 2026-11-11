package com.ensodai.avalon.domain.useCases.characterProfileScreenUseCase

import com.ensodai.avalon.data.db.models.character.CharacterProfile
import com.ensodai.avalon.data.db.models.character.CharacterProfileTuple
import com.ensodai.avalon.domain.repository.local.LocalCharacterRepository
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import com.ensodai.avalon.domain.useCases.commonUseCase.CharacterSaver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class GetCharacterProfileByIdFlowUseCase(
    private val localCharacterRepository: LocalCharacterRepository,
    private val characterRpcService: CharacterRpcService,
    private val characterSaver: CharacterSaver,
    private val getSharedMediaUseCase: GetSharedMediaUseCase
) {

    operator fun invoke(characterId: Uuid?): Flow<CharacterProfile?> {
        if (characterId == null) return flowOf(null)

        return channelFlow {
            val producerScope = this

            val localJob = producerScope.launch {
                localCharacterRepository.getCharacterProfileFlow(characterId)
                    .collect { tuple ->
                        send(tuple?.toProfile())
                    }
            }

            // 2. В фоне обновляем данные с сервера
            producerScope.launch {
                runCatching {
                    characterRpcService.getCharacter(characterPublicId = characterId)
                }.fold(
                    onSuccess = { character ->
                        characterSaver.save(character)
                        getSharedMediaUseCase.updateServerMedia(characterId, character.sharedMedia)
                    },
                    onFailure = {
                        it.printStackTrace()
                    }
                )
            }

            // Держим поток открытым, пока жива подписка на БД
            localJob.join()
        }
    }

    private fun CharacterProfileTuple.toProfile(): CharacterProfile {
        return CharacterProfile(
            id = character.id,
            name = character.name,
            ownerNick = character.ownerNick,
            canEdit = character.canEdit,
            isPublicFullDescription = character.isPublicFullDescription,
            shortDescription = character.shortDescription,
            fullDescription = character.fullDescription,
            avatarUrl = avatar?.imageUrl,
            images = images,
            relationshipLevel = character.relationshipLevel,
            experiencePoints = character.experiencePoints,
            visibility = character.visibility,
            scenario = character.scenario,
            mesExample = character.mesExample
        )
    }
}