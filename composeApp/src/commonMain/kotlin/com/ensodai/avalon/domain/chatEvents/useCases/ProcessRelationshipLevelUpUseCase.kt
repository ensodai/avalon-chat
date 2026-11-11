package com.ensodai.avalon.domain.chatEvents.useCases

import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import com.ensodai.avalon.di.ApplicationScope
import com.ensodai.avalon.domain.global.GlobalEventBus
import com.ensodai.avalon.domain.global.GlobalEvent
import com.ensodai.avalon.domain.repository.local.LocalCharacterRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Single
class ProcessRelationshipLevelUpUseCase(
    private val localCharacterRepository: LocalCharacterRepository,
    private val globalEventBus: GlobalEventBus,
    private val characterRpcService: CharacterRpcService,
    @ApplicationScope private val externalScope: CoroutineScope
) {
    suspend fun updateRelationship(characterId: Uuid, oldLevel: Int, level: Int, experience: Long) {
        localCharacterRepository.updateCharacterRelationship(
            characterId = characterId,
            level = level,
            experience = experience
        )
        globalEventBus.emit(
            GlobalEvent.RelationshipLevelUp(
                characterId = characterId,
                oldLevel = oldLevel,
                newLevel = level
            )
        )

        externalScope.launch {
            runCatching {
                characterRpcService.getCharacterEventsRoadmap(characterId)
            }.onSuccess { networkRoadmap ->
                localCharacterRepository.saveCharacterRoadmap(characterId, networkRoadmap)
            }.onFailure { e ->
                e.printStackTrace()
            }
        }
    }
}
