package com.ensodai.avalon.domain.useCases.mainScreenUseCase

import com.ensodai.avalon.domain.messageSender.TypingStatusManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class GetAllTypingCharactersIdsFlowUseCase(
    private val typingStatusManager: TypingStatusManager
) {

    fun get(): Flow<Set<Uuid>> {
        return typingStatusManager.typingChatIds.map { it.keys }
    }

}