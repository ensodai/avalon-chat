package com.ensodai.avalon.domain.global


import com.ensodai.shared.avalon.gifts.models.GiftEffectType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class GlobalEventBus {
    private val _events = MutableSharedFlow<GlobalEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    suspend fun emit(event: GlobalEvent) {
        _events.emit(event)
    }

    fun tryEmit(event: GlobalEvent) {
        _events.tryEmit(event)
    }
}

sealed class GlobalEvent {
    data class ForceUpdateRequired(
        val targetVersion: String
    ) : GlobalEvent()

    data class RelationshipLevelUp(
        val characterId: Uuid,
        val oldLevel: Int,
        val newLevel: Int
    ) : GlobalEvent()

    data class GiftEffectTriggered(
        val characterId: Uuid,
        val effectType: GiftEffectType
    ) : GlobalEvent()
}
