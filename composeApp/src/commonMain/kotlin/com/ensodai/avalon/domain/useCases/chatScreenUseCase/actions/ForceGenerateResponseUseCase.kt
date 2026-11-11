package com.ensodai.avalon.domain.useCases.chatScreenUseCase.actions

import com.ensodai.avalon.domain.repository.local.LocalChatSessionsRepository
import com.ensodai.avalon.domain.chatEvents.LocalChatAction
import com.ensodai.avalon.domain.chatEvents.ChatSyncRpcClient
import com.ensodai.avalon.utils.AppLogger
import com.ensodai.avalon.utils.getUTCTimeNow
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class ForceGenerateResponseUseCase(
    private val chatSyncRpcClient: ChatSyncRpcClient,
    private val localChatSessionsRepository: LocalChatSessionsRepository,
    private val logger: AppLogger,
) {

    suspend fun force(
        characterId: Uuid,
        llmModelCode: String?
    ) {
        logger.d("ForceGenerateResponseUseCase", "Force generate characterId: $characterId")

        val session = localChatSessionsRepository.getSessionByCharacterId(characterId)

        val sessionId: Uuid
        val variantId: Uuid
        val deviceTime = getUTCTimeNow()

        if (session == null) {
            // СЦЕНАРИЙ 1: Полностью новый чат
            sessionId = Uuid.random()
            variantId = Uuid.random()

            logger.d(
                "ForceGenerateResponseUseCase",
                "✨ Creating NEW Session and Variant locally (Lazy)"
            )

            localChatSessionsRepository.createChatSessionAndChatVariant(
                chatSessionId = sessionId,
                chatVariantId = variantId,
                characterId = characterId,
                createdAt = deviceTime
            )

        } else if (session.activeChatVariantId == null) {
            // СЦЕНАРИЙ 2: Сессия есть, но активный вариант потерян (Edge Case)
            sessionId = session.id
            variantId = Uuid.random()

            logger.d(
                "ForceGenerateResponseUseCase",
                "🛠 Session exists, but creating NEW Variant locally"
            )

            localChatSessionsRepository.createLocalChatVariantAndSetAsActive(
                sessionId = sessionId,
                newVariantId = variantId,
                createdAt = deviceTime
            )

        } else {
            // СЦЕНАРИЙ 3: Всё есть, штатный режим
            sessionId = session.id
            variantId = session.activeChatVariantId
        }

        val action = LocalChatAction.ForceStartGenerateResponse(
            chatVariantId = variantId,
            chatSessionId = sessionId,
            characterId = characterId,
            llmModelCode = llmModelCode
        )

        chatSyncRpcClient.sendAction(action)
    }

}
