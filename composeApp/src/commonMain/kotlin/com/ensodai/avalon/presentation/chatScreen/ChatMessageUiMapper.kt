package com.ensodai.avalon.presentation.chatScreen

import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.shared.avalon.chat.models.MessageRole
import com.ensodai.shared.avalon.chat.models.AudioPayload
import com.ensodai.shared.avalon.chat.models.ImagePayload
import com.ensodai.shared.avalon.chat.models.SystemEventPayload
import com.ensodai.shared.avalon.chat.models.TextPayload
import com.ensodai.shared.avalon.chat.models.StickerGiftPayload
import com.ensodai.shared.avalon.chat.models.SystemEventType
import com.ensodai.avalon.domain.audio.TtsPlayerState
import com.ensodai.avalon.domain.models.message.ChatItem
import com.ensodai.avalon.domain.models.message.MessageItem
import com.ensodai.avalon.domain.models.message.WelcomeCardItem
import com.ensodai.avalon.domain.useCases.chatScreenUseCase.chat.ParseRpgMessageUseCase
import com.ensodai.avalon.presentation.chatScreen.viewState.ChatItemUiState
import com.ensodai.avalon.presentation.chatScreen.viewState.MessageActionsConfigUiState
import com.ensodai.avalon.presentation.chatScreen.viewState.MessageBodyUiItem
import com.ensodai.avalon.presentation.chatScreen.viewState.MessageContainerUiState
import com.ensodai.avalon.theme.models.ChatRoleplayPreset
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class ChatMessageUiMapper(
    private val parseRpgMessageUseCase: ParseRpgMessageUseCase
) {

    fun map(
        items: List<ChatItem>,
        preset: ChatRoleplayPreset,
        currentPlayingContentId: Uuid?,
        ttsPlayerState: TtsPlayerState,
        generatingTtsMessageIds: Set<Uuid>
    ): List<ChatItemUiState> {
        return items.map { item ->
            when (item) {
                is WelcomeCardItem -> ChatItemUiState.WelcomeCard(
                    id = item.id,
                    welcomeInfo = item.welcomeInfo
                )
                is MessageItem -> {
                    val slot = item.slot
                    
                    val containersUi = slot.containers.map { container ->
                        val textContent = container.content.firstOrNull { it.type == AvalonMessageContentType.TEXT }
                        
                        val systemInstructionContent = container.content.firstOrNull { it.type == AvalonMessageContentType.MESSAGE_SYSTEM_INSTRUCTION }
                        val systemInstructionText = (systemInstructionContent?.payload as? TextPayload)?.text

                        val isTtsGenerating = textContent?.id != null && textContent.id in generatingTtsMessageIds
                        val isTtsPlayingOrLoading = textContent?.id != null && currentPlayingContentId == textContent.id && 
                            (ttsPlayerState is TtsPlayerState.Playing || ttsPlayerState is TtsPlayerState.Loading || ttsPlayerState is TtsPlayerState.Paused)
                        val hasAudio = container.content.any { it.type == AvalonMessageContentType.AUDIO }
                        val displayAsAudio = hasAudio || isTtsGenerating || isTtsPlayingOrLoading
                        
                        val systemEventPayload = container.content
                            .firstNotNullOfOrNull { it.payload as? SystemEventPayload }

                        val bodyItems = buildList {
                            var audioFound = false
                            container.content.forEach { contentModel ->
                                when (val payload = contentModel.payload) {
                                    is TextPayload -> {
                                        if (contentModel.type != AvalonMessageContentType.MESSAGE_SYSTEM_INSTRUCTION) {
                                            add(
                                                MessageBodyUiItem.Text(
                                                    textId = contentModel.id,
                                                    rawText = payload.text,
                                                    parsedBlocks = parseRpgMessageUseCase(payload.text),
                                                    isStreaming = container.isStreaming
                                                )
                                            )
                                        }
                                    }
                                    is ImagePayload -> {
                                        add(
                                            MessageBodyUiItem.Image(
                                                imageId = contentModel.id,
                                                payload = payload
                                            )
                                        )
                                    }
                                    is AudioPayload -> {
                                        add(
                                            MessageBodyUiItem.Audio(
                                                audioId = contentModel.id,
                                                payload = payload,
                                                isPlaying = currentPlayingContentId == contentModel.id && ttsPlayerState is TtsPlayerState.Playing
                                            )
                                        )
                                        audioFound = true
                                    }
                                    is SystemEventPayload -> {
                                        add(
                                            MessageBodyUiItem.SystemEvent(
                                                systemEventId = contentModel.id,
                                                text = "",
                                                systemEventType = payload.eventType
                                            )
                                        )
                                    }
                                    is StickerGiftPayload -> {
                                        add(
                                            MessageBodyUiItem.StickerGift(
                                                url = payload.url
                                            )
                                        )
                                    }
                                }
                            }

                            if (displayAsAudio && !audioFound && textContent != null) {
                                add(
                                    MessageBodyUiItem.Audio(
                                        audioId = textContent.id,
                                        payload = AudioPayload(id = Uuid.parse("00000000-0000-0000-0000-000000000000"), url = ""),
                                        isPlaying = currentPlayingContentId == textContent.id && ttsPlayerState is TtsPlayerState.Playing
                                    )
                                )
                            }
                        }

                        val showStatus = container.role == MessageRole.USER
                        
                        val actionsConfig = when {
                            container.showOutreachFeedback -> MessageActionsConfigUiState.Feedback
                            systemEventPayload != null -> {
                                when (systemEventPayload.eventType) {
                                    SystemEventType.QUOTA_EXCEEDED,
                                    SystemEventType.CUSTOM_KEY_INVALID,
                                    SystemEventType.CUSTOM_MODEL_MISSING,
                                    SystemEventType.CUSTOM_INSUFFICIENT_CREDITS,
                                    SystemEventType.CUSTOM_MODEL_NOT_FOUND,
                                    SystemEventType.CUSTOM_CONTEXT_LENGTH_EXCEEDED,
                                    SystemEventType.CUSTOM_PROVIDER_UNAVAILABLE -> {
                                        MessageActionsConfigUiState.Single(systemEventPayload.eventType)
                                    }
                                    else -> MessageActionsConfigUiState.None
                                }
                            }
                            else -> MessageActionsConfigUiState.None
                        }

                        val showExternalPlayButton = container.role == MessageRole.MODEL && textContent != null && !displayAsAudio
                        val isTtsLoading = currentPlayingContentId == textContent?.id && ttsPlayerState is TtsPlayerState.Loading

                        val rawText = (textContent?.payload as? TextPayload)?.text.orEmpty()

                        MessageContainerUiState(
                            id = container.id,
                            role = container.role,
                            rarity = container.rarity,
                            status = container.status,
                            isRead = container.isRead,
                            bodyItems = bodyItems,
                            userReaction = container.userReaction,
                            characterReaction = container.characterReaction,
                            showStatus = showStatus,
                            actionsConfig = actionsConfig,
                            showExternalPlayButton = showExternalPlayButton,
                            isTtsLoading = isTtsLoading,
                            rawText = rawText,
                            systemInstructionText = systemInstructionText,
                            isStreaming = container.isStreaming
                        )
                    }

                    val activeContainerId = slot.activeContainerId ?: slot.containers.firstOrNull()?.id ?: Uuid.parse("00000000-0000-0000-0000-000000000000")

                    ChatItemUiState.Message(
                        id = item.id,
                        slotId = slot.slotId,
                        activeContainerId = activeContainerId,
                        slot = slot,
                        containers = containersUi,
                        chatRoleplayPreset = preset
                    )
                }
            }
        }
    }
}
