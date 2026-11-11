package com.ensodai.avalon.domain.useCases.chatScreenUseCase

import com.ensodai.avalon.data.db.models.message.SlotInfo
import com.ensodai.avalon.domain.repository.local.LocalChatHistoryRepository
import com.ensodai.avalon.domain.repository.local.LocalChatSessionsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class GetLastMessageSlotFlowUseCase(
    private val localChatHistoryRepository: LocalChatHistoryRepository,
    private val localChatSessionsRepository: LocalChatSessionsRepository,
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun get(characterId: Uuid): Flow<SlotInfo?> {
        return localChatSessionsRepository.getActiveChatVariantFlow(characterId)
            .distinctUntilChanged()
            .flatMapLatest { variantId ->
                if (variantId == null) {
                    flowOf(null)
                } else {
                    localChatHistoryRepository.getLastSlotInfoFlow(variantId)
                }
            }
        // Можно добавить flowOn(IODispatcher), если репозитории не переключают контекст сами
    }
}