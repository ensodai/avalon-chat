package com.ensodai.avalon.domain.sessionWorker

import com.ensodai.avalon.data.db.dto.AppUserDto
import com.ensodai.avalon.data.db.dto.CharacterAvatarDto
import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.data.db.dto.ChatPreviewMessageDto
import com.ensodai.avalon.data.db.dto.ChatSessionPreviewDto
import com.ensodai.avalon.data.db.dto.ChatVariantDto
import com.ensodai.avalon.data.db.dto.ChatVariantImageDto
import com.ensodai.avalon.data.db.dto.PersonaDto
import com.ensodai.avalon.data.db.dto.WelcomeCardDto
import com.ensodai.avalon.data.db.dto.WelcomeCardImageDto
import com.ensodai.shared.avalon.auth.models.AppUserResponse
import com.ensodai.shared.avalon.character.models.CharacterResponse
import com.ensodai.shared.avalon.character.models.CharacterPreviewResponse
import com.ensodai.shared.avalon.chat.models.ChatSessionPreviewResponse
import com.ensodai.shared.avalon.chat.models.ChatVariantResponse
import com.ensodai.shared.avalon.persona.models.PersonaResponse
import com.ensodai.shared.avalon.sync.models.SyncResponse
import com.ensodai.shared.avalon.sync.models.SyncChangesRequest
import com.ensodai.shared.avalon.sync.models.LlmConfigResponse
import com.ensodai.shared.avalon.rpc.services.SyncRpcService
import com.ensodai.avalon.data.repository.local.DatastoreManager
import com.ensodai.avalon.domain.models.SaveCharacterProfileDto
import com.ensodai.avalon.domain.repository.local.LocalCacheRepository
import com.ensodai.avalon.domain.repository.local.LocalCharacterRepository
import com.ensodai.avalon.domain.repository.local.LocalChatHistoryRepository
import com.ensodai.avalon.domain.repository.local.LocalChatSessionsRepository
import com.ensodai.avalon.domain.repository.local.LocalUserRepository
import com.ensodai.avalon.domain.chatEvents.ChatSyncRpcClient
import com.ensodai.avalon.domain.repository.local.SaveCharacterBulkData
import com.ensodai.avalon.utils.AppLogger
import com.ensodai.avalon.utils.IODispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class SyncManager(
    private val syncRpcService: SyncRpcService,
    private val datastoreManager: DatastoreManager,
    private val localCharacterRepository: LocalCharacterRepository,
    private val localChatSessionsRepository: LocalChatSessionsRepository,
    private val localUserRepository: LocalUserRepository,
    private val chatSyncRpcClient: ChatSyncRpcClient,
    private val localChatHistoryRepository: LocalChatHistoryRepository,
    private val localCacheRepository: LocalCacheRepository,
    private val logger: AppLogger
) {
    private val syncMutex = Mutex()

    private val scope = CoroutineScope(IODispatcher + SupervisorJob())

    /**
     * Основной метод, запускающий "догоняющую" синхронизацию.
     * Безопасен для вызова из любого места (например, ViewModel).
     */
    suspend fun performCatchUpSync() {

        syncMutex.withLock {
            logger.d("SyncManager", "Starting catch-up sync...")

            val lastSyncTimestamp = datastoreManager.getLastSyncTimestampFlow().firstOrNull()
            logger.d("SyncManager", "Last sync timestamp is: $lastSyncTimestamp")

            val response = runCatching {
                syncRpcService.syncChanges(SyncChangesRequest(since = lastSyncTimestamp))
            }

            response.fold(
                onSuccess = { result ->
                    // ВАЖНАЯ ПРОВЕРКА (см. пункт 3)
                    if (isEmptyChanges(result)) {
                        logger.d("SyncManager", "🏁 Changes empty. Sync finished.")
                        datastoreManager.saveLastSyncTimestamp(result.latestTimestamp)
                        return@withLock
                    }

                    applyChangesToDatabase(result)
                    datastoreManager.saveLastSyncTimestamp(result.latestTimestamp)
                    logger.d(
                        "SyncManager",
                        "Sync successful. New timestamp: ${result.latestTimestamp}"
                    )
                },
                onFailure = { exception ->
                    logger.e("SyncManager", "Sync failed: ${exception.stackTraceToString()}")
                }
            )
        }

    }

    /**
     * Применяет полученные с сервера изменения к локальной базе данных.
     */
    private suspend fun applyChangesToDatabase(response: SyncResponse) {
        // 1. Сначала работаем с базой
        updateCharacters(characters = response.updatedCharacters)
        updatePersona(personas = response.updatedUserPersonas)
        updateUser(user = response.updatedAppUser)
        deleteCharacters(characters = response.deletedCharacterIds)

        updateChatsData(
            sessions = response.updatedChatSessions,
            variants = response.updatedChatVariants,
            deletedVariantIds = response.deletedChatVariantIds,
            deletedSessionIds = response.deletedChatSessionIds
        )
        hardDeleteSlots(sloIds = response.deletedSlots)
        updateAvailableLLmConfigs(availableLlm = response.availableLlm)
        
        // 2. Очистка очереди (Вмешательство в RAM)
        if (response.deletedChatVariantIds.isNotEmpty() || response.deletedChatSessionIds.isNotEmpty()) {
            chatSyncRpcClient.cancelMessagesForDeletedContexts(
                deletedSessionIds = response.deletedChatSessionIds,
                deletedVariantIds = response.deletedChatVariantIds
            )
        }
    }

    private suspend fun updateUser(user: AppUserResponse?) {
        val appUser = user?.let {
            AppUserDto(
                id = it.id,
                name = it.name,
                nickName = it.nickName,
                avatarUrl = it.avatarUrl ?: "",
                currentQuotaLimit = it.currentQuotaLimit,
                maxQuotaLimit = it.maxQuotaLimit,
                defaultPersonaId = it.defaultPersonaId,
                reserveBalance = it.reserveBalance,
                maxReserveBalance = it.maxReserveBalance,
                premiumStatus = it.premiumStatus,
                paymentProvider = it.paymentProvider,
                nextBillingDate = it.nextBillingDate,
                isOutreachEnabled = it.isOutreachEnabled,
                dailyOutreachLimit = it.dailyOutreachLimit,
                isFlowModeEnabled = it.isFlowModeEnabled
            )
        }
        localUserRepository.updateUser(appUser = appUser)
    }

    private suspend fun updatePersona(personas: List<PersonaResponse>) {
        val userPersonas = personas.map { personas ->
            PersonaDto(
                id = personas.id,
                name = personas.name,
                description = personas.description,
                createdAt = personas.createdAt
            )
        }
        localUserRepository.updatePersonas(personas = userPersonas)
    }

    private suspend fun updateCharacters(characters: List<CharacterResponse>) {
        if (characters.isEmpty()) return

        val bulkData = characters.map { character ->
            val avatar = character.avatar?.let {
                CharacterAvatarDto(
                    imagePublicId = it.id,
                    url = it.imageUrl,
                    thumbHash = it.thumbHash
                )
            }
            val images = character.images.map {
                CharacterImageDTO(
                    imagePublicId = it.id,
                    url = it.imageUrl,
                    thumbHash = it.thumbHash
                )
            }
            val welcomeCardImages = character.welcomeCard?.welcomeCardImages?.map {
                WelcomeCardImageDto(
                    imagePublicId = it.id,
                    url = it.imageUrl,
                    thumbHash = it.thumbHash
                )
            }
            val welcomeCard = character.welcomeCard?.let { welcomeCard ->
                WelcomeCardDto(
                    id = welcomeCard.id,
                    welcomeCardDescription = welcomeCard.welcomeCardDescription,
                    welcomeCardImages = welcomeCardImages,
                    startColor = welcomeCard.startColor,
                    endColor = welcomeCard.endColor,
                    colorLight = welcomeCard.cardColorLight,
                    isEnableLight = welcomeCard.isEnableLight
                )
            }

            val saveCharacterProfileDto = SaveCharacterProfileDto(
                characterName = character.characterName,
                fullDescription = character.fullDescription,
                shortDescription = character.shortDescription,
                updatedAt = character.updatedAt,
                visibility = character.visibility,
                firstMessage = character.firstMessage,
                welcomeCard = welcomeCard,
                userCanEditCharacter = character.canEdit,
                isPublicFullDescription = character.isPublicFullDescription,
                temperature = character.temperature,
                unclaimedEnergy = character.unclaimedEnergy,
                totalEarnedEnergy = character.totalEarnedEnergy
            )

            SaveCharacterBulkData(
                characterId = character.id,
                ownerNick = character.ownerNick,
                characterAvatar = avatar,
                characterImages = images,
                saveCharacterProfileDto = saveCharacterProfileDto,
                welcomeCardImages = welcomeCardImages,
            )
        }

        localCharacterRepository.upsertCharactersBulk(bulkData)
    }

    private suspend fun deleteCharacters(characters: List<Uuid>) {
        localCharacterRepository.deleteCharacters(characters = characters)
    }

    private suspend fun updateChatsData(
        sessions: List<ChatSessionPreviewResponse>,
        variants: List<ChatVariantResponse>,
        deletedVariantIds: List<Uuid>,
        deletedSessionIds: List<Uuid>
    ) {
        val localCharacterIds = localCharacterRepository.getAllCharacterIds().toSet()

        val validSessions = sessions.filter { session ->
            val hasCharacter = localCharacterIds.contains(session.characterId)
            if (!hasCharacter) {
                logger.e(
                    "SyncManager",
                    "Skip session ${session.id}: Character ${session.characterId} not found locally!"
                )
            }
            hasCharacter
        }

        val newSessionCharMap = validSessions.associate { it.characterId to it.id }

        val conflictingOldSessionIds =
            localChatSessionsRepository.findConflictingSessions(newSessionCharMap.keys)
                .filter { (oldSessionId, charId) ->
                    val newSessionId = newSessionCharMap[charId]
                    newSessionId != null && newSessionId != oldSessionId
                }
                .map { it.sessionId }

        if (conflictingOldSessionIds.isNotEmpty()) {
            localChatSessionsRepository.hardDeleteSessions(conflictingOldSessionIds)
        }

        val safeSessionDtos = validSessions.map { dto ->
            val lastMessage = dto.lastMessage?.let { message ->
                ChatPreviewMessageDto(
                    id = message.id,
                    type = message.type,
                    content = message.content,
                    role = message.role,
                    createdAt = message.createdAt
                )
            }
            ChatSessionPreviewDto(
                id = dto.id,
                characterId = dto.characterId,
                isArchived = dto.isArchived,
                updatedAt = dto.updatedAt,
                createdAt = dto.createdAt,
                lastMessage = lastMessage,
                activeChatVariantId = dto.activeVariantId
            ).copy(activeChatVariantId = null)
        }

        if (safeSessionDtos.isNotEmpty()) {
            localChatSessionsRepository.upsertChatSessions(safeSessionDtos)
        }

        val existingSessionIds = localChatSessionsRepository.getAllSessionIds()

        val safeVariantDtos = variants.mapNotNull { dto ->
            if (!existingSessionIds.contains(dto.sessionId)) {
                logger.w(
                    "SyncManager",
                    "Skipping variant ${dto.id}: Parent Session ${dto.sessionId} not found locally."
                )
                return@mapNotNull null
            }

            val chatVariantImageDto = dto.image?.let {
                ChatVariantImageDto(id = it.id, imageUrl = it.imageUrl, thumbHash = it.thumbHash)
            }
            ChatVariantDto(
                id = dto.id,
                name = dto.name,
                sessionId = dto.sessionId,
                image = chatVariantImageDto,
                unreadCount = dto.unreadCount,
                createdAt = dto.createdAt,
                updatedAt = dto.updatedAt
            )
        }

        if (safeVariantDtos.isNotEmpty()) {
            localChatSessionsRepository.upsertChatVariants(safeVariantDtos)
        }

        validSessions.forEach { dto ->
            val targetVariantId = dto.activeVariantId
            if (targetVariantId != null) {
                val isVariantExists = localChatHistoryRepository.isVariantExists(targetVariantId)

                if (isVariantExists) {
                    localChatSessionsRepository.updateActiveSessionVariant(dto.id, targetVariantId)
                }
            }
        }

        if (deletedVariantIds.isNotEmpty()) {
            localChatSessionsRepository.hardDeleteChatVariants(deletedVariantIds)
        }

        val sessionsToDelete = deletedSessionIds - conflictingOldSessionIds.toSet()
        if (sessionsToDelete.isNotEmpty()) {
            localChatSessionsRepository.hardDeleteSessions(sessionsToDelete)
        }
    }

    private suspend fun hardDeleteSlots(sloIds: List<Uuid>) {
        localChatHistoryRepository.hardDeleteSlotWithMessagesBySlotIds(sloIds)
    }

    private suspend fun updateAvailableLLmConfigs(availableLlm: List<LlmConfigResponse>) {
        localCacheRepository.updateAvailableLlmConfigs(availableLlm)
    }
}

private fun isEmptyChanges(response: SyncResponse): Boolean {
    return response.updatedCharacters.isEmpty() &&
            response.updatedUserPersonas.isEmpty() &&
            response.updatedAppUser == null &&
            response.updatedChatSessions.isEmpty() &&
            response.updatedChatVariants.isEmpty() &&
            response.deletedCharacterIds.isEmpty() &&
            response.deletedChatSessionIds.isEmpty() &&
            response.deletedChatVariantIds.isEmpty() &&
            response.deletedSlots.isEmpty()
}
