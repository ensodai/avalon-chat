package app.domain.chat

import app.data.network.openai.OpenAiException
import app.data.network.openai.toSystemEventType
import com.ensodai.shared.avalon.gifts.models.*
import com.ensodai.shared.avalon.chat.wss.ServerMessage
import org.koin.core.annotation.Single

import app.service.chat.*
import app.domain.chat.*
import app.data.db.tables.LlmConfig
import app.data.repository.CharacterRepository
import app.service.*
import app.service.chat.*
import app.service.rarityMessageService.RarityService
import app.service.relationship.CharacterRelationshipService
import com.ensodai.shared.avalon.chat.models.Rarity
import app.data.models.AICharacterConfigLLMDto
import app.data.models.UserEnergyBalance
import app.service.lorebook.model.EntryTimedState
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.shared.avalon.chat.models.MessageRole
import com.ensodai.shared.avalon.chat.models.SystemEventType
import com.ensodai.shared.avalon.chat.models.StickerGiftPayload
import com.ensodai.shared.avalon.chat.services.MessageContentCodec
import app.domain.models.DomainMessage
import app.data.repository.ChatRepository
import app.data.repository.GiftsRepository
import app.domain.chat.ChatContextService
import app.utils.getUTCTimeNow
import com.ensodai.shared.avalon.chat.models.MessageContainerResponse
import com.ensodai.shared.avalon.chat.models.MessageSlotResponse
import app.utils.logger.WssLogger
import com.ensodai.shared.avalon.relationship.RelationshipLevelCalculator
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import kotlin.coroutines.cancellation.CancellationException
import kotlin.uuid.Uuid

@Single
class ChatGenerationService(
    private val giftsRepository: GiftsRepository,
    private val chatRepository: ChatRepository,
    private val historyBuilder: HistoryBuilder,
    private val llmService: LlmService,
    private val messageNotifier: MessageNotifier,
    private val rarityService: RarityService,
    private val characterRepository: CharacterRepository,
    private val generationJobManager: GenerationJobManager,
    private val chatContextService: ChatContextService,
    private val chatSummaryService: ChatSummaryService,
    private val imageGenerationService: ImageGenerationService,
    private val energyService: EnergyService,
    private val typingIndicatorService: TypingIndicatorService,
    private val characterRelationshipService: CharacterRelationshipService,
    private val userRepositoryApp: app.data.repository.UserRepositoryApp
) {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Остановка генерации.
     */
    fun stop(chatSessionId: Uuid) {
        WssLogger.info("Стоп генерации")
        generationJobManager.stopJob(chatSessionId)
    }

    /**
     * Запуск процесса генерации.
     * Возвращает Job, но она уже зарегистрирована в менеджере.
     */
    suspend fun launchGenerationJob(
        userId: Uuid,
        chatSessionId: Uuid,
        variantId: Uuid,
        characterId: Uuid,
        preValidatedConfig: AICharacterConfigLLMDto?,
        targetSlotId: Uuid?,
        llmConfig: LlmConfig,
        energyBalance: UserEnergyBalance,
        userPremiumStatus: PremiumStatus,
        energyHistoryRecordId: Uuid? = null
    ) {
        val rarity = rarityService.determineNextRarityForUser(userId)

        // Запускаем Job в applicationScope (переживет разрыв сокета)
        val generationJob = applicationScope.launch {
            val typingLease = typingIndicatorService.acquire(
                userId = userId,
                chatSessionId = chatSessionId,
                rarity = rarity
            )

            try {
                // 2. Дебаунс (защита от спама кнопки Stop/Start)
                delay(1500L)

                // 3. Получение свежего контекста (всегда перечитываем из БД, чтобы учесть повышение уровня после сохранения сообщения)
                val finalConfig = chatContextService.getOrCreateContext(
                    userId = userId,
                    chatSessionId = chatSessionId,
                    chatVariantId = variantId,
                    characterId = characterId,
                    llmConfig = llmConfig
                )

                if (finalConfig == null) {
                    return@launch
                }

                // 4. Получение уровня отношений для динамической памяти
                val relationshipLevel = characterRelationshipService.getRelationshipLevel(userId, characterId)
                val maxHistorySize = RelationshipLevelCalculator.getHistorySizeForLevel(relationshipLevel)

                // 5. Сборка истории
                val rawHistory = if (targetSlotId != null) {
                    chatRepository.getHistoryBeforeSlot(variantId, targetSlotId, limit = maxHistorySize)
                } else {
                    chatRepository.getHistoryForVariant(variantId, limit = maxHistorySize)
                }

                val triggerMessage = rawHistory.messages.lastOrNull { it.role == MessageRole.USER }
                val giftPayload = triggerMessage?.contents
                    ?.firstOrNull { it.type == AvalonMessageContentType.GIFT }
                    ?.let { contentPart ->
                        try {
                            MessageContentCodec.decode(
                                type = AvalonMessageContentType.GIFT,
                                contentString = contentPart.content,
                            ) as? StickerGiftPayload
                        } catch (e: Exception) {
                            null
                        }
                    }

                if (giftPayload != null && (giftPayload.actionType == GiftActionType.CAMERA_SELFIE || giftPayload.actionType == GiftActionType.CAMERA_PHOTO)) {
                    // --- СЦЕНАРИЙ: ГЕНЕРАЦИЯ КАРТИНКИ ---
                    WssLogger.info("Обнаружен триггер подарка (${giftPayload.actionType}). Запуск генерации картинки.")

                    val imageResult = imageGenerationService.generateImage(
                        aICharacterConfigLLMDto = finalConfig,
                        giftName = giftPayload.giftName,
                        history = rawHistory.messages,
                        actionType = giftPayload.actionType
                    )

                    // ID сообщения с подарком для обновления relatedMessageId в EnergyHistory
                    val triggerMessageId = triggerMessage.id

                    processImageResult(
                        imageResult = imageResult,
                        userId = userId,
                        chatSessionId = chatSessionId,
                        variantId = variantId,
                        characterId = characterId,
                        targetSlotId = targetSlotId,
                        rarity = rarity,
                        energyBalance = energyBalance,
                        energyHistoryRecordId = energyHistoryRecordId,
                        triggerMessageId = triggerMessageId
                    )

                    if (imageResult is ImageGenerationResult.Success && finalConfig.pendingEventDirective != null) {
                        generateFollowUpTextEvent(
                            userId = userId,
                            chatSessionId = chatSessionId,
                            variantId = variantId,
                            characterId = characterId,
                            finalConfig = finalConfig,
                            llmConfig = llmConfig,
                            rarity = rarity,
                            energyBalance = energyBalance,
                            relationshipLevel = relationshipLevel,
                            maxHistorySize = maxHistorySize
                        )
                    }
                } else {
                    // --- СЦЕНАРИЙ: ГЕНЕРАЦИЯ ТЕКСТА (LLM) ---
                    val preparedHistory = historyBuilder.build(
                        firstMessage = finalConfig.firstMessage,
                        conversationSummary = finalConfig.conversationSummary,
                        rawHistory = rawHistory.messages,
                        maxHistorySize = maxHistorySize,
                        lorebookAtDepth = finalConfig.lorebookAtDepth
                    )

                    var eventScenario = finalConfig.pendingEventDirective
                    if (giftPayload != null && giftPayload.actionType == GiftActionType.TEXT_EVENT) {
                        val gift = giftsRepository.getGiftById(giftPayload.giftId)
                        if (gift != null) {
                            val emotionalDirective = getEmotionalDirective(gift.price, relationshipLevel)
                            eventScenario = """
                                Event: The user has sent you a gift named "${gift.giftName}" (${gift.giftDescription}).
                                
                                Instructions:
                                1. Generate a reaction to this gift on behalf of your character.
                                2. Play out the receipt of this specific item naturally.
                                3. $emotionalDirective
                                4. Your response must provide a deep emotional impact to the user. Strictly maintain your Tone of Voice.
                            """.trimIndent()
                            WssLogger.info("Обнаружен триггер текстового подарка (${gift.giftName}). Сценарий сформирован.")
                        }
                    }

                    // Проверяем включен ли Flow Mode
                    val isFlowModeEnabled = userRepositoryApp.isFlowModeEnabled(userId)

                    if (isFlowModeEnabled) {
                        val flow = llmService.generateResponseStream(
                            aICharacterConfigLLMDto = finalConfig,
                            history = preparedHistory,
                            messageRarity = rarity,
                            totalMessageCount = rawHistory.totalCount,
                            relationshipLevel = relationshipLevel,
                            eventScenario = eventScenario
                        )

                        processStreamResult(
                            stream = flow,
                            userId = userId,
                            chatSessionId = chatSessionId,
                            variantId = variantId,
                            characterId = characterId,
                            targetSlotId = targetSlotId,
                            rarity = rarity,
                            energyBalance = energyBalance,
                            rawHistory = rawHistory.messages,
                            energyHistoryRecordId = energyHistoryRecordId,
                            modelCode = llmConfig.code,
                            loreTimedStates = finalConfig.nextLoreTimedStates
                        )
                    } else {
                        // Вызов LLM в классическом режиме
                        val llmResult = llmService.generateResponse(
                            aICharacterConfigLLMDto = finalConfig,
                            history = preparedHistory,
                            messageRarity = rarity,
                            totalMessageCount = rawHistory.totalCount,
                            relationshipLevel = relationshipLevel,
                            eventScenario = eventScenario
                        )

                        // Обработка результата
                        processTextResult(
                            llmResult = llmResult,
                            userId = userId,
                            chatSessionId = chatSessionId,
                            variantId = variantId,
                            characterId = characterId,
                            targetSlotId = targetSlotId,
                            rarity = rarity,
                            energyBalance = energyBalance,
                            rawHistory = rawHistory.messages,
                            energyHistoryRecordId = energyHistoryRecordId,
                            modelCode = llmConfig.code,
                            loreTimedStates = finalConfig.nextLoreTimedStates
                        )
                    }
                }

                // Запускаем фоновую саммаризацию
                chatSummaryService.launchCheckAndSummarize(
                    chatVariantId = variantId,
                    userPremiumStatus = userPremiumStatus
                )
            } catch (e: CancellationException) {
                WssLogger.fail("Generation CANCELLED: ${e.localizedMessage}")
                throw e
            } catch (e: Exception) {
                WssLogger.fail("CRITICAL ERROR in generation: ${e.localizedMessage}")
                messageNotifier.notifyUserWithError(userId, "Error: ${e.message}")
            } finally {
                withContext(NonCancellable) {
                    typingLease.release()
                }
            }
        }

        // Регистрируем джобу
        generationJobManager.registerJob(chatSessionId, generationJob)
    }

    /**
     * Обработка результата генерации изображения.
     */
    private suspend fun processImageResult(
        imageResult: ImageGenerationResult,
        userId: Uuid,
        chatSessionId: Uuid,
        variantId: Uuid,
        characterId: Uuid,
        targetSlotId: Uuid?,
        rarity: Rarity,
        energyBalance: UserEnergyBalance,
        energyHistoryRecordId: Uuid?,
        triggerMessageId: Uuid? = null
    ) {
        when (imageResult) {
            is ImageGenerationResult.Success -> {
                if (targetSlotId != null) {
                    // Добавление варианта в существующий слот (свайп)
                    val encodeText = MessageContentCodec.encode(
                        type = AvalonMessageContentType.IMAGE,
                        payload = imageResult.imagePayload
                    )
                    val newContainer = chatRepository.addMessageContainer(
                        slotId = targetSlotId,
                        text = encodeText,
                        role = MessageRole.MODEL,
                        rarity = rarity,
                        contentType = AvalonMessageContentType.IMAGE,
                        visualDescription = imageResult.description // Описание для ИИ
                    )

                    messageNotifier.notifyUserWithMessage(
                        userId = userId,
                        message = ServerMessage.NewContainerAdded(
                            container = newContainer,
                            sessionId = chatSessionId,
                            currentUserQuota = energyBalance.dailyEnergy,
                            reserveEnergy = energyBalance.reserveEnergy
                        ),
                        excludeSessionId = null
                    )
                } else {
                    // Новый ход (обычная отправка подарка)
                    val encodeText = MessageContentCodec.encode(
                        type = AvalonMessageContentType.IMAGE,
                        payload = imageResult.imagePayload
                    )
                    val modelSlotResponse = chatRepository.createNewMessageTurn(
                        messageRole = MessageRole.MODEL,
                        messageRarity = rarity,
                        slotId = Uuid.random(),
                        contentId = Uuid.random(),
                        containerId = Uuid.random(),
                        chatVariantId = variantId,
                        chatSessionId = chatSessionId,
                        contentType = AvalonMessageContentType.IMAGE,
                        text = encodeText,
                        messageSystemInstruction = null,
                        visualDescription = imageResult.description
                    )

                    messageNotifier.notifyUserWithMessage(
                        userId = userId,
                        message = ServerMessage.NewChatMessage(
                            characterId = characterId,
                            slot = modelSlotResponse,
                            currentUserQuota = energyBalance.dailyEnergy,
                            reserveEnergy = energyBalance.reserveEnergy
                        ),
                        excludeSessionId = null
                    )
                }

                // Обновление статистики по токенам в EnergyHistory для генерации картинки
                WssLogger.info("Image gen: energyHistoryRecordId = $energyHistoryRecordId")
                energyHistoryRecordId?.let { recordId ->
                    // Суммируем токены подзапросов (analyzer + renderer)
                    val tokenUsage = imageResult.tokenUsage
                    WssLogger.info("Image gen: Вызываем updateTokenStats для recordId=$recordId, cost=50, model=imagen-4.0-fast-generate-001, tokens=$tokenUsage")
                    energyService.updateTokenStats(
                        recordId = recordId,
                        totalTokens = tokenUsage.totalTokens,
                        promptTokens = tokenUsage.promptTokens,
                        outputTokens = tokenUsage.outputTokens,
                        cachedTokens = tokenUsage.cachedTokens,
                        thoughtsTokens = tokenUsage.thoughtsTokens,
                        modelName = "imagen-4.0-fast-generate-001",
                        cost = 50, // Стоимость генерации картинки (энергия)
                        modelCode = "gemini_flash_2.5_lite", // Токены подзапросов считаются по цене Gemini
                        fixedCostUsd = BigDecimal("0.02"), // Фиксированная цена за изображение Imagen
                        relatedMessageId = triggerMessageId // Обновляем relatedMessageId на ID сообщения
                    )
                }
            }

            is ImageGenerationResult.Error -> {
                val errorReasonText = imageResult.exception?.message ?: imageResult.reason.name
                WssLogger.fail("Image generation failed: $errorReasonText")
                val eventType = when (imageResult.reason) {
                    ImageGenerationResult.Error.ErrorReason.PERSON_GENERATION_UNSUPPORTED -> SystemEventType.IMAGE_PERSON_BLOCKED
                    ImageGenerationResult.Error.ErrorReason.SAFETY_VIOLATION -> SystemEventType.IMAGE_SAFETY_BLOCKED
                    else -> SystemEventType.SERVER_ERROR
                }
                val systemMessage = chatRepository.createSystemMessage(
                    userId = userId,
                    chatSessionId = chatSessionId,
                    chatVariantId = variantId,
                    eventType = eventType
                )
                notifySystemMessage(userId, characterId, systemMessage, energyBalance)
            }
        }
    }

    private suspend fun processTextResult(
        llmResult: LlmResult,
        userId: Uuid,
        chatSessionId: Uuid,
        variantId: Uuid,
        characterId: Uuid,
        targetSlotId: Uuid?,
        rarity: Rarity,
        energyBalance: UserEnergyBalance,
        rawHistory: List<DomainMessage>,
        energyHistoryRecordId: Uuid? = null,
        modelCode: String? = null,  // Код модели для расчёта стоимости
        loreTimedStates: Map<Uuid, EntryTimedState> = emptyMap()
    ) {
        when (llmResult) {
            is LlmResult.Success -> {
                WssLogger.inbound(userId.toString(), "Generation Success")

                // 1. СОХРАНЯЕМ ВСЁ ОДНИМ ВЫЗОВОМ (Твой новый метод в репозитории)
                // Он вернет dbResult.reactionEvent и dbResult.responseMessageData
                val dbResult = chatRepository.saveAiResponseWithReaction(
                    userId = userId,
                    chatSessionId = chatSessionId,
                    chatVariantId = variantId,
                    targetSlotId = targetSlotId,
                    text = llmResult.text,
                    reaction = llmResult.reaction,
                    rarity = rarity,
                    contentType = AvalonMessageContentType.TEXT,
                    visualDescription = null,
                    loreTimedStates = loreTimedStates
                )

                // 2. ОТПРАВЛЯЕМ УВЕДОМЛЕНИЯ (Теперь они suspend, идут строго друг за другом)

                // 2.1. Сначала реакция
                dbResult.reactionEvent?.let { reactionMsg ->
                    messageNotifier.notifyUserWithMessage(
                        userId = userId,
                        message = reactionMsg,
                        excludeSessionId = null
                    )
                }

                // 2.2. Потом основное сообщение (Текст)
                val socketMessage = when (val data = dbResult.responseMessageData) {
                    is MessageSlotResponse -> ServerMessage.NewChatMessage(
                        characterId = characterId,
                        slot = data,
                        currentUserQuota = energyBalance.dailyEnergy,
                        reserveEnergy = energyBalance.reserveEnergy
                    )
                    is MessageContainerResponse -> ServerMessage.NewContainerAdded(
                        container = data,
                        sessionId = chatSessionId,
                        currentUserQuota = energyBalance.dailyEnergy,
                        reserveEnergy = energyBalance.reserveEnergy
                    )
                    else -> null
                }

                socketMessage?.let {
                    messageNotifier.notifyUserWithMessage(
                        userId = userId,
                        message = it,
                        excludeSessionId = null
                    )
                }

                // 3. ОБНОВЛЯЕМ СТАТИСТИКУ
                characterRepository.incrementStats(
                    characterId = characterId,
                    tokensDelta = llmResult.tokens,
                    userId = userId
                )
                
                // 4. ОЧИЩАЕМ PENDING EVENT (если был отыгран)
                characterRelationshipService.clearPendingEvent(userId, characterId)
                characterRelationshipService.addExperience(userId, characterId, 10L)

                // Обновление статистики по токенам в EnergyHistory
                energyHistoryRecordId?.let { recordId ->
                    llmResult.tokenStats?.let { stats ->
                        energyService.updateTokenStats(
                            recordId = recordId,
                            totalTokens = stats.totalTokens,
                            promptTokens = stats.promptTokens,
                            outputTokens = stats.outputTokens,
                            cachedTokens = stats.cachedTokens,
                            thoughtsTokens = stats.thoughtsTokens,
                            modelCode = modelCode,
                            openRouterCostUsd = stats.openRouterCostUsd
                        )
                    }
                }
            }

            is LlmResult.Blocked -> {
                WssLogger.fail("Generation Blocked: ${llmResult.reason}")
                val eventType = when (llmResult.reason) {
                    "INVALID_API_KEY", "API_KEY_REQUIRED" -> SystemEventType.CUSTOM_KEY_INVALID
                    "INSUFFICIENT_CREDITS" -> SystemEventType.CUSTOM_INSUFFICIENT_CREDITS
                    "RATE_LIMIT_EXCEEDED" -> SystemEventType.CUSTOM_RATE_LIMIT
                    "MODEL_NOT_FOUND" -> SystemEventType.CUSTOM_MODEL_NOT_FOUND
                    "CONTEXT_LENGTH_EXCEEDED" -> SystemEventType.CUSTOM_CONTEXT_LENGTH_EXCEEDED
                    "PROVIDER_UNAVAILABLE" -> SystemEventType.CUSTOM_PROVIDER_UNAVAILABLE
                    else -> SystemEventType.MESSAGE_BLOCKED
                }
                val systemMessage = chatRepository.createSystemMessage(
                    userId = userId,
                    chatSessionId = chatSessionId,
                    chatVariantId = variantId,
                    eventType = eventType
                )
                notifySystemMessage(userId, characterId, systemMessage, energyBalance)
            }

            is LlmResult.Error -> {
                WssLogger.fail("Generation Error: ${llmResult.exception.message}")
                val systemMessage = chatRepository.createSystemMessage(
                    userId = userId,
                    chatSessionId = chatSessionId,
                    chatVariantId = variantId,
                    eventType = SystemEventType.SERVER_ERROR
                )
                notifySystemMessage(userId, characterId, systemMessage, energyBalance)
            }
        }
    }

    private suspend fun processStreamResult(
        stream: kotlinx.coroutines.flow.Flow<LlmStreamChunk>,
        userId: Uuid,
        chatSessionId: Uuid,
        variantId: Uuid,
        characterId: Uuid,
        targetSlotId: Uuid?,
        rarity: Rarity,
        energyBalance: UserEnergyBalance,
        rawHistory: List<DomainMessage>,
        energyHistoryRecordId: Uuid? = null,
        modelCode: String? = null,
        loreTimedStates: Map<Uuid, EntryTimedState> = emptyMap()
    ) {
        var isFirstChunk = true
        var slotId: Uuid? = null
        var containerId: Uuid? = null
        var contentId: Uuid? = null
        val accumulatedText = StringBuilder()
        
        var lastDbSaveTime = 0L
        var totalTokens = 0L
        var accumulatedReaction: String? = null
        
        try {
            stream.collect { chunk ->
                if (chunk.reaction != null) {
                    accumulatedReaction = chunk.reaction
                }
                if (isFirstChunk) {
                    isFirstChunk = false
                    
                    // Создаем пустышку в БД синхронно
                    val dbResult = chatRepository.saveAiResponseWithReaction(
                        userId = userId,
                        chatSessionId = chatSessionId,
                        chatVariantId = variantId,
                        targetSlotId = targetSlotId,
                        text = MessageContentCodec.encode(
                            type = AvalonMessageContentType.TEXT,
                            payload = com.ensodai.shared.avalon.chat.models.TextPayload(text = "")
                        ),
                        reaction = null,
                        rarity = rarity,
                        contentType = AvalonMessageContentType.TEXT,
                        visualDescription = null,
                        loreTimedStates = loreTimedStates
                    )
                    
                    val socketMessage = when (val data = dbResult.responseMessageData) {
                        is MessageSlotResponse -> {
                            slotId = data.id
                            containerId = data.activeContainerId
                            contentId = data.containers.firstOrNull { it.containerId == containerId }?.contents?.firstOrNull()?.id
                            ServerMessage.NewChatMessage(
                                characterId = characterId,
                                slot = data,
                                currentUserQuota = energyBalance.dailyEnergy,
                                reserveEnergy = energyBalance.reserveEnergy
                            )
                        }
                        is MessageContainerResponse -> {
                            slotId = data.slotId
                            containerId = data.containerId
                            contentId = data.contents.firstOrNull()?.id
                            ServerMessage.NewContainerAdded(
                                container = data,
                                sessionId = chatSessionId,
                                currentUserQuota = energyBalance.dailyEnergy,
                                reserveEnergy = energyBalance.reserveEnergy
                            )
                        }
                        else -> null
                    }
                    
                    // Шлем пустой слот/контейнер юзеру
                    socketMessage?.let {
                        messageNotifier.notifyUserWithMessage(userId, it, null)
                    }
                }
                
                if (chunk.textDelta.isNotEmpty()) {
                    accumulatedText.append(chunk.textDelta)
                    
                    // Шлем в вебсокет дельту
                    if (slotId != null && containerId != null && contentId != null) {
                        messageNotifier.notifyUserWithMessage(
                            userId = userId,
                            message = ServerMessage.MessageStreamChunk(
                                characterId = characterId,
                                sessionId = chatSessionId,
                                variantId = variantId,
                                slotId = slotId!!,
                                containerId = containerId!!,
                                contentId = contentId!!,
                                textChunk = chunk.textDelta
                            ),
                            excludeSessionId = null
                        )
                        
                        // Дебаунс сохранение в БД (раз в 1 сек)
                        val now = System.currentTimeMillis()
                        if (now - lastDbSaveTime > 1000) {
                            chatRepository.updateAiMessageText(contentId!!, MessageContentCodec.encode(
                                type = AvalonMessageContentType.TEXT,
                                payload = com.ensodai.shared.avalon.chat.models.TextPayload(text = accumulatedText.toString())
                            ))
                            lastDbSaveTime = now
                        }
                    }
                }
                
                if (chunk.isFinished) {
                    // Финальное сохранение с реакцией
                    if (slotId != null && contentId != null && containerId != null) {
                        val (cleanAccumulatedText, extractedReaction) = app.service.chat.ReactionParser.parseFullText(accumulatedText.toString())
                        val effectiveReaction = chunk.reaction ?: accumulatedReaction ?: extractedReaction

                        val reactionEvent = chatRepository.finalizeAiMessageWithReaction(
                            userId = userId,
                            chatSessionId = chatSessionId,
                            chatVariantId = variantId,
                            slotId = slotId!!,
                            contentId = contentId!!,
                            finalText = MessageContentCodec.encode(
                                type = AvalonMessageContentType.TEXT,
                                payload = com.ensodai.shared.avalon.chat.models.TextPayload(text = cleanAccumulatedText)
                            ),
                            reaction = effectiveReaction
                        )
                        
                        // Отправляем реакцию в вебсокет если есть
                        reactionEvent?.let { reactionMsg ->
                            messageNotifier.notifyUserWithMessage(userId, reactionMsg, null)
                        }
                        
                        // Сообщаем клиенту, что генерация завершена (MessageUpdated)
                        messageNotifier.notifyUserWithMessage(
                            userId = userId,
                            message = ServerMessage.MessageUpdated(
                                containerId = containerId!!,
                                newText = cleanAccumulatedText,
                                updatedAt = app.utils.getUTCTimeNow(),
                                sessionId = chatSessionId,
                                isLastMessage = true
                            ),
                            excludeSessionId = null
                        )
                    }
                    
                    // Обновляем статистику
                    val tokens = chunk.tokenStats?.totalTokens ?: (accumulatedText.length / 4).toLong()
                    characterRepository.incrementStats(characterId, tokens, userId)
                    characterRelationshipService.clearPendingEvent(userId, characterId)
                    characterRelationshipService.addExperience(userId, characterId, 10L)
                    
                    // Обновляем energy history
                    energyHistoryRecordId?.let { recordId ->
                        val stats = chunk.tokenStats ?: LlmResult.TokenStats(tokens, 0, tokens)
                        energyService.updateTokenStats(
                            recordId = recordId,
                            totalTokens = stats.totalTokens,
                            promptTokens = stats.promptTokens,
                            outputTokens = stats.outputTokens,
                            cachedTokens = stats.cachedTokens,
                            thoughtsTokens = stats.thoughtsTokens,
                            modelCode = modelCode,
                            openRouterCostUsd = stats.openRouterCostUsd
                        )
                    }
                }
            }
        } catch (e: Exception) {
            if (e is OpenAiException) {
                val eventType = e.toSystemEventType()
                WssLogger.fail("Stream OpenAiException: [${e::class.simpleName}] ${e.message}")
                val systemMessage = chatRepository.createSystemMessage(userId, chatSessionId, variantId, eventType)
                notifySystemMessage(userId, characterId, systemMessage, energyBalance)
                return
            }

            if (e.message == "BLOCKED_PROMPT" || e.message == "BLOCKED_CONTENT") {
                WssLogger.fail("Stream Blocked: ${e.message}")
                val systemMessage = chatRepository.createSystemMessage(userId, chatSessionId, variantId, SystemEventType.MESSAGE_BLOCKED)
                notifySystemMessage(userId, characterId, systemMessage, energyBalance)
                return
            }
            
            // Если ошибка в процессе стриминга, но что-то уже накопилось
            if (slotId != null && containerId != null && contentId != null) {
                messageNotifier.notifyUserWithMessage(
                    userId = userId,
                    message = ServerMessage.MessageStreamChunk(
                        characterId = characterId,
                        sessionId = chatSessionId,
                        variantId = variantId,
                        slotId = slotId!!,
                        containerId = containerId!!,
                        contentId = contentId!!,
                        textChunk = ""
                    ),
                    excludeSessionId = null
                )
                
                // Сообщаем клиенту, что генерация завершена (с ошибкой, но мы сохраняем что есть)
                messageNotifier.notifyUserWithMessage(
                    userId = userId,
                    message = ServerMessage.MessageUpdated(
                        containerId = containerId!!,
                        newText = accumulatedText.toString(),
                        updatedAt = getUTCTimeNow(),
                        sessionId = chatSessionId,
                        isLastMessage = true
                    ),
                    excludeSessionId = null
                )
                
                chatRepository.updateAiMessageText(contentId!!, accumulatedText.toString())
                
                val tokens = (accumulatedText.length / 4).toLong()
                characterRepository.incrementStats(characterId, tokens, userId)
            } else {
                // Ничего не накопилось, отдаем как Blocking Error
                WssLogger.fail("Stream Error before first chunk: ${e.message}")
                val systemMessage = chatRepository.createSystemMessage(userId, chatSessionId, variantId, SystemEventType.SERVER_ERROR)
                notifySystemMessage(userId, characterId, systemMessage, energyBalance)
            }
        }
    }

    private suspend fun notifySystemMessage(
        userId: Uuid,
        characterId: Uuid,
        messageSlot: MessageSlotResponse,
        energyBalance: UserEnergyBalance
    ) {
        messageNotifier.notifyUserWithMessage(
            userId = userId,
            excludeSessionId = null,
            message = ServerMessage.NewChatMessage(
                characterId = characterId,
                slot = messageSlot,
                currentUserQuota = energyBalance.dailyEnergy,
                reserveEnergy = energyBalance.reserveEnergy
            )
        )
    }

    private suspend fun generateFollowUpTextEvent(
        userId: Uuid,
        chatSessionId: Uuid,
        variantId: Uuid,
        characterId: Uuid,
        finalConfig: AICharacterConfigLLMDto,
        llmConfig: LlmConfig,
        rarity: Rarity,
        energyBalance: UserEnergyBalance,
        relationshipLevel: Int,
        maxHistorySize: Int
    ) {
        WssLogger.info("[Follow-Up Event] Запуск автогенерации сюжета после отправки подарка")
        try {
            // Небольшая задержка для имитации "печатания" сообщения-пояснения после картинки
            delay(1500L)

            // Загружаем историю, которая уже включает отправленную картинку
            val updatedRawHistory = chatRepository.getHistoryForVariant(variantId, limit = maxHistorySize)

            val preparedHistory = historyBuilder.build(
                firstMessage = finalConfig.firstMessage,
                conversationSummary = finalConfig.conversationSummary,
                rawHistory = updatedRawHistory.messages,
                maxHistorySize = maxHistorySize,
                lorebookAtDepth = finalConfig.lorebookAtDepth
            )

            val llmResult = llmService.generateResponse(
                aICharacterConfigLLMDto = finalConfig,
                history = preparedHistory,
                messageRarity = rarity,
                totalMessageCount = updatedRawHistory.totalCount,
                relationshipLevel = relationshipLevel,
                eventScenario = finalConfig.pendingEventDirective
            )

            processTextResult(
                llmResult = llmResult,
                userId = userId,
                chatSessionId = chatSessionId,
                variantId = variantId,
                characterId = characterId,
                targetSlotId = null, // Сюжетное текстовое сообщение идет новым ходом
                rarity = rarity,
                energyBalance = energyBalance,
                rawHistory = updatedRawHistory.messages,
                energyHistoryRecordId = null, // Это автоматическое сюжетное сообщение бесплатно
                modelCode = llmConfig.code,
                loreTimedStates = finalConfig.nextLoreTimedStates
            )
        } catch (e: Exception) {
            WssLogger.fail("[Follow-Up Event] Ошибка во время автогенерации сюжетного текста: ${e.localizedMessage}")
        }
    }

    private fun getEmotionalDirective(price: Int, relationshipLevel: Int): String {
        return if (relationshipLevel <= 2) {
            when {
                price < 100 ->
                    "The character should react with a polite and slightly distant thankfulness, as they are not very close to the user yet."
                price in 100..499 ->
                    "The character should feel surprised and slightly shy/blushing, wondering why the user gave them such a nice gift when they barely know each other."
                else ->
                    "The character should be deeply confused, defensive, but highly flustered/blushing by such unexpected generosity from a near-stranger."
            }
        } else {
            when {
                price < 100 ->
                    "The character should react with a warm, cute thankfulness and a sweet smile."
                price in 100..499 ->
                    "The character should feel genuine joy, comfort, and open affection. This gift is warm and meaningful to them."
                else ->
                    "The character should be completely overwhelmed with love, intense blushing, and profound gratitude, highlighting how incredibly important this gesture is to them."
            }
        }
    }

    @Serializable
    private data class LlmJsonResponse(
        val text: String,
        val reaction: String? = null
    )
}
