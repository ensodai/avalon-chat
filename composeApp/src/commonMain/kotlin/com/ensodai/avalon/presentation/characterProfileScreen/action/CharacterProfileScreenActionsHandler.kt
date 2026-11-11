package com.ensodai.avalon.presentation.characterProfileScreen.action

import androidx.lifecycle.viewModelScope
import com.ensodai.shared.avalon.character.models.CharacterCardExportFormat
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.shared.avalon.gifts.models.PremiumStatus
import com.ensodai.avalon.data.db.cache.AvailableLlmConfigs
import com.ensodai.avalon.domain.useCases.characterProfileScreenUseCase.GetCharacterProfileByIdFlowUseCase
import com.ensodai.avalon.domain.useCases.characterProfileScreenUseCase.GetSharedMediaUseCase
import com.ensodai.avalon.domain.useCases.characterProfileScreenUseCase.ReportCharacterUseCase
import com.ensodai.avalon.domain.useCases.commonUseCase.GetChatStatusUseCase
import com.ensodai.avalon.domain.useCases.commonUseCase.GetCharacterByIdUseCase
import com.ensodai.avalon.domain.useCases.chatScreenUseCase.SelectActiveLlmConfigForChatUseCase
import com.ensodai.avalon.domain.useCases.exportCharacterCardUseCase.ExportCharacterCardUseCase
import com.ensodai.avalon.domain.useCases.paymentSreenUseCase.GetUserTariffUseCase
import com.ensodai.avalon.presentation.characterProfileScreen.CharacterProfileScreenViewModel
import com.ensodai.avalon.presentation.characterProfileScreen.viewState.CharacterAvatarWithSeed
import com.ensodai.avalon.presentation.paymentScreen.viewState.UserTariff
import com.ensodai.avalon.utils.IODispatcher
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

fun CharacterProfileScreenViewModel.createViewState(
    characterId: Uuid,
    getCharacterById: GetCharacterProfileByIdFlowUseCase
) {
    viewModelScope.launch(IODispatcher) {
        getCharacterById(characterId)
            .distinctUntilChanged()
            .collectLatest { character ->
                if (character != null) {
                    val publicAvatars = mutableListOf<CharacterAvatarWithSeed>()
                    if (character.avatarUrl != null) {
                        publicAvatars.add(
                            CharacterAvatarWithSeed(
                                id = null,
                                imageUrl = character.avatarUrl,
                                seed = character.id.toString() + character.name,
                                isPrivate = false,
                                isUnlocked = true
                            )
                        )
                    }
                    character.images.filter { !it.isPrivate && it.requiredLevel == null }.forEach { img ->
                        publicAvatars.add(
                            CharacterAvatarWithSeed(
                                id = img.id,
                                imageUrl = img.imageUrl,
                                seed = character.id.toString() + character.name,
                                thumbHash = img.thumbHash,
                                isPrivate = false,
                                energyCost = 0,
                                requiredLevel = null,
                                isUnlocked = true
                            )
                        )
                    }

                    val premiumPhotos = character.images.filter { it.isPrivate || it.requiredLevel != null }.map { img ->
                        CharacterAvatarWithSeed(
                            id = img.id,
                            imageUrl = img.imageUrl,
                            seed = character.id.toString() + character.name,
                            thumbHash = img.thumbHash,
                            isPrivate = img.isPrivate,
                            energyCost = img.energyCost,
                            requiredLevel = img.requiredLevel,
                            isUnlocked = img.isUnlocked
                        )
                    }

                    val canExport = character.canEdit || (character.visibility != CharacterVisibility.PRIVATE && character.isPublicFullDescription)

                    updateViewState {
                        it.copy(
                            characterId = characterId,
                            ownerNick = character.ownerNick,
                            characterName = character.name,
                            isEnableEditProfileMode = character.canEdit,
                            canExportCard = canExport,
                            images = publicAvatars,
                            premiumImages = premiumPhotos,
                            isPublicFullDescription = character.isPublicFullDescription,
                            shortDescription = character.shortDescription,
                            fullDescription = character.fullDescription,
                            relationshipLevel = character.relationshipLevel,
                            experiencePoints = character.experiencePoints,
                            scenario = character.scenario,
                            mesExample = character.mesExample
                        )
                    }
                }
            }
    }
}


fun CharacterProfileScreenViewModel.updateTypingStatus(
    getChatStatus: GetChatStatusUseCase
) {
    viewModelScope.launch {
        getChatStatus(characterId)
            .collect { chatStatus ->
                updateViewState { currentState ->
                    currentState.copy(
                        typingStatus = chatStatus,
                    )
                }
            }
    }
}

fun CharacterProfileScreenViewModel.showReportDialog() {
    updateViewState {
        it.copy(
            reportCharacterState = it.reportCharacterState.copy(
                isShowReportDialog = true
            )
        )
    }
}

fun CharacterProfileScreenViewModel.closeReportDialog() {
    updateViewState {
        it.copy(
            reportCharacterState = it.reportCharacterState.copy(
                isShowReportDialog = false
            )
        )
    }
}

fun CharacterProfileScreenViewModel.changeReportMessage(message: String) {
    updateViewState {
        it.copy(
            reportCharacterState = it.reportCharacterState.copy(
                reportMessage = message
            )
        )
    }
}

fun CharacterProfileScreenViewModel.reportCharacter(
    reportCharacter: ReportCharacterUseCase,
    characterId: Uuid?
) {
    if (characterId == null) return
    val reportMessage = viewState.value.reportCharacterState.reportMessage.take(600)
    viewModelScope.launch(IODispatcher) {
        reportCharacter.report(
            characterId = characterId,
            message = reportMessage
        )
        updateViewState {
            it.copy(
                reportCharacterState = it.reportCharacterState.copy(
                    isShowReportDialog = false
                )
            )
        }
    }
}

fun CharacterProfileScreenViewModel.observeSharedMedia(
    getSharedMedia: GetSharedMediaUseCase
) {
    viewModelScope.launch(IODispatcher) {
        getSharedMedia(characterId)
            .collectLatest { combinedMedia ->
                updateViewState { state ->
                    state.copy(sharedMedia = combinedMedia)
                }
            }
    }
}

fun CharacterProfileScreenViewModel.observeLlmConfigs(
    getCharacterByIdUseCase: GetCharacterByIdUseCase
) {
    viewModelScope.launch {
        getCharacterByIdUseCase(characterId)
            .distinctUntilChanged()
            .collectLatest { chatCharacter ->
                if (chatCharacter != null) {
                    updateViewState {
                        it.copy(
                            availableLlmConfigs = chatCharacter.availableLlmConfigs,
                            selectedConfig = chatCharacter.selectedConfig
                        )
                    }
                }
            }
    }
}

fun CharacterProfileScreenViewModel.selectActiveLlmConfig(
    selectActiveLlmConfigForChat: SelectActiveLlmConfigForChatUseCase,
    config: AvailableLlmConfigs
) {
    viewModelScope.launch {
        selectActiveLlmConfigForChat.select(characterId, config)
    }
}

fun CharacterProfileScreenViewModel.fetchUserTariff(
    getUserTariff: GetUserTariffUseCase
) {
    viewModelScope.launch(IODispatcher) {
        val currentInfo = getUserTariff.get()
        val currentStatus = currentInfo?.premiumStatus ?: PremiumStatus.FREE
        val currentTariff = when (currentStatus) {
            PremiumStatus.FREE -> UserTariff.Free
            PremiumStatus.SUPPORTER -> UserTariff.Supporter
            PremiumStatus.PREMIUM -> UserTariff.Premium
            PremiumStatus.CRYSTAL -> UserTariff.Crystal
            PremiumStatus.ELITE -> UserTariff.Elite
            PremiumStatus.SPECIAL -> UserTariff.Special
        }
        updateViewState { it.copy(userTariff = currentTariff) }
    }
}

fun CharacterProfileScreenViewModel.exportCharacterCard(
    exportCharacterCardUseCase: ExportCharacterCardUseCase,
    format: CharacterCardExportFormat
) {
    viewModelScope.launch(IODispatcher) {
        updateViewState { it.copy(isExporting = true, exportErrorMessage = null, exportSuccessMessage = null) }
        val result = exportCharacterCardUseCase.exportAndSave(characterId, format)
        result.fold(
            onSuccess = { savedPath ->
                updateViewState {
                    it.copy(
                        isExporting = false,
                        exportSuccessMessage = "Карточка сохранена: $savedPath"
                    )
                }
            },
            onFailure = { e ->
                updateViewState {
                    it.copy(
                        isExporting = false,
                        exportErrorMessage = e.message ?: "Ошибка при экспорте карточки"
                    )
                }
            }
        )
    }
}

fun CharacterProfileScreenViewModel.dismissExportMessage() {
    updateViewState { it.copy(exportSuccessMessage = null, exportErrorMessage = null) }
}

fun CharacterProfileScreenViewModel.toggleCardFlip() {
    updateViewState { it.copy(isCardFlipped = !it.isCardFlipped) }
}

fun CharacterProfileScreenViewModel.setCardFlipped(flipped: Boolean) {
    updateViewState { it.copy(isCardFlipped = flipped) }
}

