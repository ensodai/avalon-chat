package com.ensodai.avalon.domain.useCases.characterProfileScreenUseCase

import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.avalon.domain.repository.local.LocalChatHistoryRepository
import com.ensodai.shared.avalon.chat.services.MessageContentCodec
import com.ensodai.shared.avalon.chat.models.ImagePayload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class GetSharedMediaUseCase(
    private val localChatHistoryRepository: LocalChatHistoryRepository
) {
    private val serverMediaFlows = mutableMapOf<Uuid, MutableStateFlow<List<String>>>()

    fun updateServerMedia(characterId: Uuid, media: List<String>) {
        val flow = serverMediaFlows.getOrPut(characterId) { MutableStateFlow(emptyList()) }
        flow.value = media
    }

    operator fun invoke(characterId: Uuid): Flow<List<String>> {
        val localFlow = localChatHistoryRepository.getSharedMediaFlow(characterId).map { list ->
            list.mapNotNull { contentJson ->
                val payload = MessageContentCodec.decode(AvalonMessageContentType.IMAGE, contentJson)
                (payload as? ImagePayload)?.url
            }
        }
        val serverFlow = serverMediaFlows.getOrPut(characterId) { MutableStateFlow(emptyList()) }

        return combine(localFlow, serverFlow) { local, server ->
            (local + server).distinct()
        }
    }
}
