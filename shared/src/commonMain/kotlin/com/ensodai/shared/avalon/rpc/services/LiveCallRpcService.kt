package com.ensodai.shared.avalon.rpc.services

import com.ensodai.shared.avalon.liveCall.models.LiveCallConfig
import com.ensodai.shared.avalon.liveCall.models.LiveCallServerEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc
import kotlin.uuid.Uuid

@Rpc
interface LiveCallRpcService {
    /**
     * Запускает сессию голосового звонка.
     * @param characterId ID персонажа.
     * @param chatSessionId ID сессии чата.
     * @param chatVariantId ID варианта чата.
     * @param config Конфигурация (модель, голос, язык).
     * @param audioStream Входящий поток аудио-чанков от клиента.
     * @return Поток событий от сервера (аудио-чанки ИИ, изменения состояния).
     */
    fun startLiveCall(
        characterId: Uuid,
        chatSessionId: Uuid,
        chatVariantId: Uuid,
        config: LiveCallConfig,
        audioStream: Flow<ByteArray>
    ): Flow<LiveCallServerEvent>
}
