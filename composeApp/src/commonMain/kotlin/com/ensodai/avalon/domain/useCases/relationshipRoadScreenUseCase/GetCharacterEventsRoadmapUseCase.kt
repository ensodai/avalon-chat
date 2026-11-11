package com.ensodai.avalon.domain.useCases.relationshipRoadScreenUseCase

import com.ensodai.shared.avalon.character.models.CharacterEventRoadmapDto
import com.ensodai.avalon.domain.repository.local.LocalCharacterRepository
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Single
class GetCharacterEventsRoadmapUseCase(
    private val localCharacterRepository: LocalCharacterRepository,
    private val characterRpcService: CharacterRpcService
) {
    fun execute(characterId: Uuid): Flow<List<CharacterEventRoadmapDto>> = channelFlow {
        val producerScope = this
        
        // 1. Стримим из локальной БД
        val localJob = producerScope.launch {
            localCharacterRepository.getCharacterRoadmapFlow(characterId)
                .collect { items ->
                    send(items)
                }
        }
        
        // 2. В фоне обновляем данные с сервера
        producerScope.launch {
            runCatching {
                characterRpcService.getCharacterEventsRoadmap(characterId)
            }.onSuccess { networkRoadmap ->
                localCharacterRepository.saveCharacterRoadmap(characterId, networkRoadmap)
            }.onFailure {
                it.printStackTrace()
            }
        }
        
        localJob.join()
    }
}
