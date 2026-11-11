@file:OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)

package com.ensodai.avalon.presentation.addCharacterScreen.actions

import androidx.lifecycle.viewModelScope
import com.ensodai.avalon.data.db.dto.CategoryItemDto
import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.data.db.dto.WelcomeCardImageDto
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.shared.avalon.character.models.InteractionFormat
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import com.ensodai.avalon.domain.useCases.addCharacterScreenUseCase.GetCategoriesUseCase
import com.ensodai.avalon.domain.useCases.commonUseCase.CreateNewCharacterUseCase
import com.ensodai.avalon.domain.useCases.commonUseCase.UploadPickedImageUseCase
import com.ensodai.avalon.domain.useCases.commonUseCase.WelcomeCardConfig
import com.ensodai.shared.avalon.character.models.CreateStandaloneLorebookRequest
import com.ensodai.shared.avalon.character.models.UserLorebookSummaryDto
import com.ensodai.avalon.domain.useCases.lorebookUseCase.CreateStandaloneLorebookUseCase
import com.ensodai.avalon.domain.useCases.lorebookUseCase.GetUserLorebooksLibraryUseCase
import com.ensodai.avalon.domain.useCases.lorebookUseCase.ImportStandaloneLorebookUseCase
import com.ensodai.avalon.presentation.addCharacterScreen.AddCharacterScreenViewModel
import kotlin.io.encoding.Base64
import com.ensodai.avalon.presentation.addCharacterScreen.viewState.AddCharacterLoadingState
import com.ensodai.avalon.presentation.addCharacterScreen.viewState.CreateStages
import com.ensodai.avalon.presentation.addCharacterScreen.viewState.PendingImageUpload
import com.ensodai.avalon.presentation.addCharacterScreen.viewState.UploadedCharacterImageRef
import com.ensodai.avalon.presentation.addCharacterScreen.viewState.UploadedWelcomeCardImageRef
import com.ensodai.avalon.presentation.addCharacterScreen.viewState.hasPendingImageUploads
import com.ensodai.avalon.presentation.addCharacterScreen.viewState.isPublicFullDescription
import com.ensodai.avalon.presentation.commonComponents.welcomCard.CardColor
import com.ensodai.avalon.utils.EMPTY_STRING
import com.ensodai.avalon.utils.IODispatcher
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

fun AddCharacterScreenViewModel.createViewState(
    getCategories: GetCategoriesUseCase,
) {
    viewModelScope.launch(IODispatcher) {
        val categories = getCategories.get()
        val stagesList = listOf(
            CreateStages.NameStage,
            CreateStages.VisibleStage,
            CreateStages.AvatarStage,
            CreateStages.ShortDescriptionStage,
            CreateStages.TemperatureStage,
            CreateStages.CategoriesStage,
            CreateStages.PublishCharacterStage
        )
        updateViewState {
            it.copy(
                categories = categories,
                selectedCategory = categories.firstOrNull(),
                currentStages = CreateStages.NameStage,
                stagesList = stagesList,
            )
        }
    }
}

fun AddCharacterScreenViewModel.onCategorySelected(selectedCategory: CategoryItemDto) {
    updateViewState {
        it.copy(selectedCategory = selectedCategory)
    }
}

fun AddCharacterScreenViewModel.changeShortDescription(shortDescription: String) {
    updateViewState { currentState ->
        currentState.copy(
            isErrorShortDescription = shortDescription.isEmpty(),
            characterConfigState = currentState.characterConfigState.copy(
                shortDescription = shortDescription.take(160)
            )
        )
    }
}

fun AddCharacterScreenViewModel.changeAvailabilityOption(privacyOption: CharacterVisibility) {
    updateViewState {
        it.copy(characterConfigState = it.characterConfigState.copy(characterPrivacy = privacyOption))
    }
}

fun AddCharacterScreenViewModel.publishCharacter(createNewCharacter: CreateNewCharacterUseCase) {
    viewModelScope.launch(IODispatcher) {
        val stateBeforePublish = viewState.value
        if (stateBeforePublish.hasPendingImageUploads) {
            updateViewState {
                it.copy(
                    errorMessage = "Image upload is still in progress",
                    isShowErrorAlertDialog = true
                )
            }
            return@launch
        }

        updateViewState {
            it.copy(addCharacterLoadingState = AddCharacterLoadingState.Loading)
        }
        val currentState = viewState.value
        val visibility = currentState.characterConfigState.characterPrivacy

        createNewCharacter.create(
            characterName = currentState.characterConfigState.characterName,
            fullDescription = currentState.characterConfigState.fullDescription ?: EMPTY_STRING,
            shortDescription = currentState.characterConfigState.shortDescription,
            isPublicFullDescription = currentState.isPublicFullDescription,
            category = currentState.selectedCategory?.categoryCode,
            firstMessage = currentState.characterConfigState.firstMessage,
            avatarToken = currentState.uploadedCharacterImages
                .firstOrNull { it.image.imagePublicId == currentState.characterAvatar?.imagePublicId }
                ?.uploadToken,
            imageTokens = currentState.uploadedCharacterImages
                .filter { it.image.imagePublicId != currentState.characterAvatar?.imagePublicId }
                .map { it.uploadToken },
            welcomeCardImageTokens = currentState.uploadedWelcomeCardImages.map { it.uploadToken },
            welcomeCardConfig = WelcomeCardConfig(
                welcomeCardImages = currentState.characterConfigState.welcomeCardState.welcomeCardImages,
                welcomeCardDescription = currentState.characterConfigState.welcomeCardState.welcomeCardDescription,
                startColor = currentState.characterConfigState.welcomeCardState.startColor.key,
                endColor = currentState.characterConfigState.welcomeCardState.endColor.key,
                colorLight = currentState.characterConfigState.welcomeCardState.startColor.key,
                isEnableLight = currentState.characterConfigState.welcomeCardState.isEnableLight
            ),
            temperature = currentState.characterConfigState.temperature,
            visibility = visibility,
            isOutreachEnabled = currentState.characterConfigState.isOutreachEnabled,
            voice = currentState.characterConfigState.voice,
            interactionFormat = currentState.characterConfigState.interactionFormat,
            scenario = currentState.characterConfigState.scenario,
            mesExample = currentState.characterConfigState.mesExample,
            attachedLorebookIds = currentState.characterConfigState.attachedLorebookIds
        ).fold(
            onSuccess = {
                delay(2000)
                updateViewState { state ->
                    state.copy(
                        characterHasSaved = true,
                        addCharacterLoadingState = AddCharacterLoadingState.Success
                    )
                }
            },
            onFailure = { message ->
                updateViewState { state ->
                    state.copy(
                        errorMessage = message.message,
                        isShowErrorAlertDialog = true,
                        characterHasSaved = false,
                        addCharacterLoadingState = AddCharacterLoadingState.Error(message.message)
                    )
                }
            }
        )
    }
}

fun AddCharacterScreenViewModel.changeName(name: String) {
    updateViewState { currentState ->
        currentState.copy(
            isErrorName = name.isEmpty(),
            characterConfigState = currentState.characterConfigState.copy(
                characterName = name.take(60)
            )
        )
    }
}

fun AddCharacterScreenViewModel.goToNextStage() {
    updateViewState { currentState ->

        when (currentState.currentStages) {
            CreateStages.NameStage -> {
                if (currentState.characterConfigState.characterName.isEmpty()) {
                    return@updateViewState currentState.copy(isErrorName = true)
                }
            }

            CreateStages.VisibleStage -> {
                currentState.copy(currentStages = CreateStages.AvatarStage)
            }

            CreateStages.AvatarStage -> {}
            CreateStages.FullDescriptionStage -> {}
            CreateStages.ScenarioStage -> {}
            CreateStages.MesExampleStage -> {}
            CreateStages.LorebookStage -> {}
            CreateStages.PublishCharacterStage -> {}
            CreateStages.ShortDescriptionStage -> {
                if (currentState.characterConfigState.shortDescription.isEmpty()) {
                    return@updateViewState currentState.copy(isErrorShortDescription = true)
                }
            }

            CreateStages.TemperatureStage -> {}
            CreateStages.CategoriesStage -> {}
            CreateStages.FirstMessageStage -> {}
            CreateStages.HelloCardStage -> {}
            CreateStages.OutreachStage -> {}
        }

        val currentIndex = currentState.stagesList.indexOf(currentState.currentStages)

        if (currentIndex in 0 until currentState.stagesList.lastIndex) {
            val newStage = currentState.stagesList[currentIndex + 1]
            currentState.copy(currentStages = newStage)
        } else {
            currentState
        }
    }
}

fun AddCharacterScreenViewModel.goToPreviousStage() {
    updateViewState { currentState ->
        val currentIndex = currentState.stagesList.indexOf(currentState.currentStages)

        if (currentIndex > 0) {
            currentState.copy(currentStages = currentState.stagesList[currentIndex - 1])
        } else {
            currentState
        }
    }
}

fun AddCharacterScreenViewModel.showAdvancedStages() {
    val stagesList = listOf(
        CreateStages.NameStage,
        CreateStages.VisibleStage,
        CreateStages.AvatarStage,
        CreateStages.ShortDescriptionStage,
        CreateStages.TemperatureStage,
        CreateStages.CategoriesStage,
        CreateStages.FullDescriptionStage,
        CreateStages.ScenarioStage,
        CreateStages.MesExampleStage,
        CreateStages.LorebookStage,
        CreateStages.FirstMessageStage,
        CreateStages.HelloCardStage,
//        CreateStages.OutreachStage,
        CreateStages.PublishCharacterStage
    )
    updateViewState {
        it.copy(
            advancedSettingsHaveBeenOpened = !it.advancedSettingsHaveBeenOpened,
            stagesList = stagesList,
            currentStages = CreateStages.CategoriesStage
        )
    }
    goToNextStage()
}

fun AddCharacterScreenViewModel.changeOutreachOption(isOutreachEnabled: Boolean) {
    updateViewState {
        it.copy(characterConfigState = it.characterConfigState.copy(isOutreachEnabled = isOutreachEnabled))
    }
}

fun AddCharacterScreenViewModel.changeFullDescription(fullDescription: String) {
    updateViewState {
        it.copy(
            characterConfigState = it.characterConfigState.copy(
                fullDescription = fullDescription.take(
                    21000
                )
            )
        )
    }
}

fun AddCharacterScreenViewModel.changeScenario(scenario: String) {
    updateViewState {
        it.copy(
            characterConfigState = it.characterConfigState.copy(
                scenario = scenario.take(
                    21000
                )
            )
        )
    }
}

fun AddCharacterScreenViewModel.changeMesExample(mesExample: String) {
    updateViewState {
        it.copy(
            characterConfigState = it.characterConfigState.copy(
                mesExample = mesExample.take(
                    21000
                )
            )
        )
    }
}

fun AddCharacterScreenViewModel.changeTemperature(temperature: Float) {
    updateViewState {
        it.copy(characterConfigState = it.characterConfigState.copy(temperature = temperature))
    }
}

fun AddCharacterScreenViewModel.attachCharacterImages(
    files: List<PlatformFile>,
    uploadPickedImageUseCase: UploadPickedImageUseCase
) {
    val currentState = viewState.value
    val availableSlots =
        (50 - currentState.characterImages.size - currentState.pendingCharacterImageUploads.size)
            .coerceAtLeast(0)

    files.take(availableSlots).forEach { file ->
        val pendingId = Uuid.random().toString()
        val fileName = "image-${pendingId.take(8)}"

        updateViewState { state ->
            state.copy(
                pendingCharacterImageUploads = state.pendingCharacterImageUploads + PendingImageUpload(
                    id = pendingId,
                    fileName = fileName
                )
            )
        }

        val job = viewModelScope.launch(IODispatcher) {
            uploadPickedImageUseCase.uploadCharacterImage(file).fold(
                onSuccess = { uploaded ->
                    if (viewState.value.pendingCharacterImageUploads.none { it.id == pendingId }) {
                        return@fold
                    }
                    val imageDto = CharacterImageDTO(
                        imagePublicId = Uuid.parse(uploaded.localId),
                        url = uploaded.imageUrl,
                        thumbHash = uploaded.thumbHash
                    )
                    updateViewState { state ->
                        state.copy(
                            characterImages = state.characterImages + imageDto,
                            uploadedCharacterImages = state.uploadedCharacterImages + UploadedCharacterImageRef(
                                image = imageDto,
                                uploadToken = uploaded.uploadToken
                            ),
                            pendingCharacterImageUploads = state.pendingCharacterImageUploads.filterNot { it.id == pendingId },
                            characterAvatar = state.characterAvatar ?: imageDto
                        )
                    }
                },
                onFailure = { error ->
                    if (error is CancellationException) return@fold
                    updateViewState { state ->
                        state.copy(
                            pendingCharacterImageUploads = state.pendingCharacterImageUploads.filterNot { it.id == pendingId },
                            errorMessage = error.message,
                            isShowErrorAlertDialog = true
                        )
                    }
                }
            )
        }
        registerCharacterUploadJob(pendingId, job)
        job.invokeOnCompletion {
            clearCharacterUploadJob(pendingId)
        }
    }
}

fun AddCharacterScreenViewModel.selectImageAvatar(characterAvatar: CharacterImageDTO?) {
    updateViewState {
        it.copy(characterAvatar = characterAvatar)
    }
}

fun AddCharacterScreenViewModel.deleteImage(characterAvatarToDelete: CharacterImageDTO?) {
    updateViewState { state ->
        val currentCharacterImages = state.characterImages
        val isCurrentAvatarDeleted = state.characterAvatar == characterAvatarToDelete
        val imageIndexToDelete = currentCharacterImages.indexOf(characterAvatarToDelete)
        val updatedCharacterImages = currentCharacterImages.filter { it != characterAvatarToDelete }

        val newCharacterAvatar = if (isCurrentAvatarDeleted) {
            if (updatedCharacterImages.isNotEmpty()) {
                if (imageIndexToDelete > 0) {
                    updatedCharacterImages[imageIndexToDelete - 1]
                } else if (updatedCharacterImages.size > 1) {
                    updatedCharacterImages[0]
                } else {
                    updatedCharacterImages[0]
                }
            } else {
                null
            }
        } else {
            state.characterAvatar
        }

        state.copy(
            characterImages = updatedCharacterImages.ifEmpty { emptyList() },
            characterAvatar = newCharacterAvatar,
            uploadedCharacterImages = state.uploadedCharacterImages.filterNot { it.image == characterAvatarToDelete }
        )
    }
}

fun AddCharacterScreenViewModel.deletePendingCharacterImage(pendingId: String) {
    cancelCharacterUploadJob(pendingId)
    updateViewState { state ->
        state.copy(
            pendingCharacterImageUploads = state.pendingCharacterImageUploads.filterNot { it.id == pendingId }
        )
    }
}

fun AddCharacterScreenViewModel.onChangeFullDescriptionPrivacy(privacyOption: CharacterVisibility) {
    updateViewState {
        it.copy(characterConfigState = it.characterConfigState.copy(fullDescriptionPrivacy = privacyOption))
    }
}

fun AddCharacterScreenViewModel.onCloseAlertDialog() {
    updateViewState {
        it.copy(
            isShowErrorAlertDialog = false,
            errorMessage = null
        )
    }
}

fun AddCharacterScreenViewModel.onShowPermissionDialog() {
    updateViewState {
        it.copy(
            isShowPermissionDialog = true
        )
    }
}

fun AddCharacterScreenViewModel.onClosePermissionDialog() {
    updateViewState {
        it.copy(
            isShowPermissionDialog = false
        )
    }
}

fun AddCharacterScreenViewModel.changeFirstMessage(message: String) {
    updateViewState {
        it.copy(
            characterConfigState = it.characterConfigState.copy(firstMessage = message.take(600))
        )
    }
}

fun AddCharacterScreenViewModel.changeWelcomeCardDescription(text: String) {

    val newDescription = text.take(8000).ifBlank {
        null
    }
    updateViewState {
        it.copy(
            characterConfigState = it.characterConfigState.copy(
                welcomeCardState = it.characterConfigState.welcomeCardState.copy(
                    welcomeCardDescription = newDescription
                )
            )
        )
    }
}

fun AddCharacterScreenViewModel.attachWelcomeCardImages(
    files: List<PlatformFile>,
    uploadPickedImageUseCase: UploadPickedImageUseCase
) {
    val currentState = viewState.value
    val currentImages = currentState.characterConfigState.welcomeCardState.welcomeCardImages
    val availableSlots = (3 - currentImages.size - currentState.pendingWelcomeCardImageUploads.size)
        .coerceAtLeast(0)

    files.take(availableSlots).forEach { file ->
        val pendingId = Uuid.random().toString()
        val fileName = "image-${pendingId.take(8)}"

        updateViewState { state ->
            state.copy(
                pendingWelcomeCardImageUploads = state.pendingWelcomeCardImageUploads + PendingImageUpload(
                    id = pendingId,
                    fileName = fileName
                )
            )
        }

        val job = viewModelScope.launch(IODispatcher) {
            uploadPickedImageUseCase.uploadWelcomeCardImage(file).fold(
                onSuccess = { uploaded ->
                    if (viewState.value.pendingWelcomeCardImageUploads.none { it.id == pendingId }) {
                        return@fold
                    }
                    val imageDto = WelcomeCardImageDto(
                        imagePublicId = Uuid.parse(uploaded.localId),
                        url = uploaded.imageUrl,
                        thumbHash = uploaded.thumbHash
                    )
                    updateViewState { state ->
                        state.copy(
                            uploadedWelcomeCardImages = state.uploadedWelcomeCardImages + UploadedWelcomeCardImageRef(
                                image = imageDto,
                                uploadToken = uploaded.uploadToken
                            ),
                            pendingWelcomeCardImageUploads = state.pendingWelcomeCardImageUploads.filterNot { it.id == pendingId },
                            characterConfigState = state.characterConfigState.copy(
                                welcomeCardState = state.characterConfigState.welcomeCardState.copy(
                                    welcomeCardImages = state.characterConfigState.welcomeCardState.welcomeCardImages + imageDto
                                )
                            )
                        )
                    }
                },
                onFailure = { error ->
                    if (error is CancellationException) return@fold
                    updateViewState { state ->
                        state.copy(
                            pendingWelcomeCardImageUploads = state.pendingWelcomeCardImageUploads.filterNot { it.id == pendingId },
                            errorMessage = error.message,
                            isShowErrorAlertDialog = true
                        )
                    }
                }
            )
        }
        registerWelcomeCardUploadJob(pendingId, job)
        job.invokeOnCompletion {
            clearWelcomeCardUploadJob(pendingId)
        }
    }
}

fun AddCharacterScreenViewModel.deleteWelcomeImage(image: WelcomeCardImageDto) {
    val images = viewState.value.characterConfigState.welcomeCardState.welcomeCardImages.minus(
        image
    )
    updateViewState {
        it.copy(
            uploadedWelcomeCardImages = it.uploadedWelcomeCardImages.filterNot { ref -> ref.image == image },
            characterConfigState = it.characterConfigState.copy(
                welcomeCardState = it.characterConfigState.welcomeCardState.copy(
                    welcomeCardImages = images
                )
            )
        )
    }
}

fun AddCharacterScreenViewModel.deletePendingWelcomeImage(pendingId: String) {
    cancelWelcomeCardUploadJob(pendingId)
    updateViewState { state ->
        state.copy(
            pendingWelcomeCardImageUploads = state.pendingWelcomeCardImageUploads.filterNot { it.id == pendingId }
        )
    }
}

fun AddCharacterScreenViewModel.changeWelcomeCardStartTone(cardColor: CardColor) {
    updateViewState {
        it.copy(
            characterConfigState = it.characterConfigState.copy(
                welcomeCardState = it.characterConfigState.welcomeCardState.copy(
                    startColor = cardColor
                )
            )
        )
    }
}

fun AddCharacterScreenViewModel.changeWelcomeCardEndTone(cardColor: CardColor) {
    updateViewState {
        it.copy(
            characterConfigState = it.characterConfigState.copy(
                welcomeCardState = it.characterConfigState.welcomeCardState.copy(
                    endColor = cardColor
                )
            )
        )
    }
}

fun AddCharacterScreenViewModel.onToggleLight() {
    val currentValue = viewState.value.characterConfigState.welcomeCardState.isEnableLight
    updateViewState {
        it.copy(
            characterConfigState = it.characterConfigState.copy(
                welcomeCardState = it.characterConfigState.welcomeCardState.copy(
                    isEnableLight = !currentValue
                )
            )
        )
    }
}

fun AddCharacterScreenViewModel.openVoicePicker() {
    updateViewState {
        it.copy(isShowVoicePicker = true)
    }
}

fun AddCharacterScreenViewModel.closeVoicePicker() {
    updateViewState {
        it.copy(isShowVoicePicker = false)
    }
}

fun AddCharacterScreenViewModel.selectVoice(voice: VoiceModel) {
    updateViewState {
        it.copy(
            characterConfigState = it.characterConfigState.copy(voice = voice),
            isShowVoicePicker = false
        )
    }
}

fun AddCharacterScreenViewModel.openInteractionFormatPicker() {
    updateViewState {
        it.copy(isShowInteractionFormatPicker = true)
    }
}

fun AddCharacterScreenViewModel.closeInteractionFormatPicker() {
    updateViewState {
        it.copy(isShowInteractionFormatPicker = false)
    }
}

fun AddCharacterScreenViewModel.changeInteractionFormat(format: InteractionFormat) {
    updateViewState {
        it.copy(
            characterConfigState = it.characterConfigState.copy(interactionFormat = format),
            isShowInteractionFormatPicker = false
        )
    }
}

fun AddCharacterScreenViewModel.openAttachLorebook(
    getUserLorebooksLibraryUseCase: GetUserLorebooksLibraryUseCase
) {
    updateViewState { it.copy(isShowAttachLorebookBottomSheet = true, isLorebookActionLoading = true) }
    viewModelScope.launch(IODispatcher) {
        try {
            val library = getUserLorebooksLibraryUseCase.execute()
            updateViewState {
                it.copy(
                    userLorebooksLibrary = library,
                    isLorebookActionLoading = false
                )
            }
        } catch (_: Exception) {
            updateViewState {
                it.copy(isLorebookActionLoading = false)
            }
        }
    }
}

fun AddCharacterScreenViewModel.closeAttachLorebook() {
    updateViewState { it.copy(isShowAttachLorebookBottomSheet = false) }
}

fun AddCharacterScreenViewModel.selectLorebook(lorebookId: Uuid) {
    updateViewState { state ->
        val selected = state.userLorebooksLibrary.firstOrNull { it.id == lorebookId }
        state.copy(
            selectedLorebook = selected,
            characterConfigState = state.characterConfigState.copy(
                attachedLorebookIds = listOf(lorebookId)
            ),
            isShowAttachLorebookBottomSheet = false
        )
    }
}

fun AddCharacterScreenViewModel.detachLorebook() {
    updateViewState { state ->
        state.copy(
            selectedLorebook = null,
            characterConfigState = state.characterConfigState.copy(
                attachedLorebookIds = emptyList()
            )
        )
    }
}

fun AddCharacterScreenViewModel.openCreateLorebook() {
    updateViewState {
        it.copy(
            isShowCreateLorebookDialog = true,
            createLorebookName = EMPTY_STRING,
            createLorebookDesc = EMPTY_STRING,
            createLorebookIsPublic = true
        )
    }
}

fun AddCharacterScreenViewModel.closeCreateLorebook() {
    updateViewState { it.copy(isShowCreateLorebookDialog = false) }
}

fun AddCharacterScreenViewModel.changeCreateLorebookName(name: String) {
    updateViewState { it.copy(createLorebookName = name) }
}

fun AddCharacterScreenViewModel.changeCreateLorebookDesc(desc: String) {
    updateViewState { it.copy(createLorebookDesc = desc) }
}

fun AddCharacterScreenViewModel.toggleCreateLorebookPublic(isPublic: Boolean) {
    updateViewState { it.copy(createLorebookIsPublic = isPublic) }
}

fun AddCharacterScreenViewModel.createLorebookSubmit(
    createStandaloneLorebookUseCase: CreateStandaloneLorebookUseCase
) {
    val currentState = viewState.value
    val name = currentState.createLorebookName.trim()
    if (name.isBlank()) return

    viewModelScope.launch(IODispatcher) {
        updateViewState { it.copy(isLorebookActionLoading = true) }
        try {
            val created = createStandaloneLorebookUseCase.execute(
                CreateStandaloneLorebookRequest(
                    name = name,
                    description = currentState.createLorebookDesc.trim().ifEmpty { null },
                    isPublic = currentState.createLorebookIsPublic
                )
            )
            val summary = UserLorebookSummaryDto(
                id = created.id,
                name = created.name,
                description = created.description,
                isPublic = created.isPublic,
                isOwner = true,
                entriesCount = created.entries.size,
                avatarUrl = created.avatarUrl
            )
            updateViewState { state ->
                state.copy(
                    selectedLorebook = summary,
                    characterConfigState = state.characterConfigState.copy(
                        attachedLorebookIds = listOf(created.id)
                    ),
                    userLorebooksLibrary = listOf(summary) + state.userLorebooksLibrary.filter { it.id != created.id },
                    isShowCreateLorebookDialog = false,
                    isLorebookActionLoading = false,
                    createLorebookName = EMPTY_STRING,
                    createLorebookDesc = EMPTY_STRING
                )
            }
        } catch (e: Exception) {
            updateViewState {
                it.copy(
                    isLorebookActionLoading = false,
                    errorMessage = e.message,
                    isShowErrorAlertDialog = true
                )
            }
        }
    }
}

fun AddCharacterScreenViewModel.importLorebook(
    importStandaloneLorebookUseCase: ImportStandaloneLorebookUseCase,
    file: PlatformFile
) {
    updateViewState { it.copy(isLorebookActionLoading = true) }
    viewModelScope.launch(IODispatcher) {
        try {
            val bytes = file.readBytes()
            val base64 = Base64.Default.encode(bytes)
            val created = importStandaloneLorebookUseCase.execute(
                fileBase64 = base64,
                attachToCharacterId = null,
                isPublic = false
            ).getOrThrow()

            val summary = UserLorebookSummaryDto(
                id = created.id,
                name = created.name,
                description = created.description,
                isPublic = created.isPublic,
                isOwner = true,
                entriesCount = created.entries.size,
                avatarUrl = created.avatarUrl
            )
            updateViewState { state ->
                state.copy(
                    selectedLorebook = summary,
                    characterConfigState = state.characterConfigState.copy(
                        attachedLorebookIds = listOf(created.id)
                    ),
                    userLorebooksLibrary = listOf(summary) + state.userLorebooksLibrary.filter { it.id != created.id },
                    isShowAttachLorebookBottomSheet = false,
                    isLorebookActionLoading = false
                )
            }
        } catch (e: Exception) {
            updateViewState {
                it.copy(
                    isLorebookActionLoading = false,
                    errorMessage = e.message,
                    isShowErrorAlertDialog = true
                )
            }
        }
    }
}


