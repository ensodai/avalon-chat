package com.ensodai.avalon.presentation.addCharacterScreen.viewState

import com.ensodai.avalon.data.db.dto.CategoryItemDto
import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.shared.avalon.character.models.InteractionFormat
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import com.ensodai.avalon.presentation.commonComponents.welcomCard.WelcomeCardState
import com.ensodai.avalon.presentation.core.BaseViewState
import com.ensodai.avalon.utils.EMPTY_STRING

import com.ensodai.shared.avalon.character.models.UserLorebookSummaryDto
import kotlin.uuid.Uuid

data class AddCharacterScreenViewState(
    val currentStages: CreateStages = CreateStages.NameStage,
    val stagesList: List<CreateStages> = emptyList(),
    val characterConfigState: CharacterConfigState = CharacterConfigState(),
    val advancedSettingsHaveBeenOpened: Boolean = false,
    val characterAvatar: CharacterImageDTO? = null,
    val characterImages: List<CharacterImageDTO> = emptyList(),
    val uploadedCharacterImages: List<UploadedCharacterImageRef> = emptyList(),
    val pendingCharacterImageUploads: List<PendingImageUpload> = emptyList(),
    val uploadedWelcomeCardImages: List<UploadedWelcomeCardImageRef> = emptyList(),
    val pendingWelcomeCardImageUploads: List<PendingImageUpload> = emptyList(),
    val isShowPermissionDialog: Boolean = false,
    val isErrorName: Boolean = false,
    val isErrorShortDescription: Boolean = false,
    val characterHasSaved: Boolean = false,
    val categories: List<CategoryItemDto>? = null,
    val selectedCategory: CategoryItemDto? = null,
    val isShowErrorAlertDialog: Boolean = false,
    val errorMessage: String? = null,
    val addCharacterLoadingState: AddCharacterLoadingState = AddCharacterLoadingState.Default,
    val isShowVoicePicker: Boolean = false,
    val isShowInteractionFormatPicker: Boolean = false,
    val userLorebooksLibrary: List<UserLorebookSummaryDto> = emptyList(),
    val selectedLorebook: UserLorebookSummaryDto? = null,
    val isShowAttachLorebookBottomSheet: Boolean = false,
    val isShowCreateLorebookDialog: Boolean = false,
    val createLorebookName: String = EMPTY_STRING,
    val createLorebookDesc: String = EMPTY_STRING,
    val createLorebookIsPublic: Boolean = true,
    val isLorebookActionLoading: Boolean = false
) : BaseViewState()

sealed class AddCharacterLoadingState {
    object Default : AddCharacterLoadingState()
    object Loading : AddCharacterLoadingState()
    class Error(errorMessage: String?) : AddCharacterLoadingState()
    object Success : AddCharacterLoadingState()
}

data class CharacterConfigState(
    val characterName: String = EMPTY_STRING,
    val characterPrivacy: CharacterVisibility = CharacterVisibility.PUBLIC,
    val fullDescriptionPrivacy: CharacterVisibility = CharacterVisibility.PUBLIC,
    val shortDescription: String = EMPTY_STRING,
    val fullDescription: String? = null,
    val scenario: String? = null,
    val mesExample: String? = null,
    val temperature: Float = 0.5f,
    val firstMessage: String? = null,
    val welcomeCardState: WelcomeCardState = WelcomeCardState(),
    val isOutreachEnabled: Boolean = false,
    val voice: VoiceModel = VoiceModel.ZEPHYR,
    val interactionFormat: InteractionFormat = InteractionFormat.LITERARY_RPG,
    val attachedLorebookIds: List<Uuid> = emptyList()
)

val AddCharacterScreenViewState.isPublicFullDescription: Boolean
    get() = characterConfigState.fullDescriptionPrivacy == CharacterVisibility.PUBLIC

val AddCharacterScreenViewState.isShowContinueButton: Boolean
    get() = currentStages != CreateStages.PublishCharacterStage

val AddCharacterScreenViewState.isShowBackButton: Boolean
    get() = stagesList.indexOf(currentStages) > 0


val AddCharacterScreenViewState.isShowPublishButton: Boolean
    get() = currentStages == CreateStages.PublishCharacterStage

val AddCharacterScreenViewState.isShowAdvancesButton: Boolean
    get() = currentStages == CreateStages.PublishCharacterStage && !advancedSettingsHaveBeenOpened

val AddCharacterScreenViewState.imagesWithAvatarOnTop: List<CharacterImageDTO>
    get() {
        val avatar = this.characterAvatar
        val images = this.characterImages

        if (avatar == null || !images.contains(avatar)) {
            return images
        }

        val mutableImages = images.toMutableList()
        mutableImages.remove(avatar)
        mutableImages.add(0, avatar)

        return mutableImages.toList()
    }

val AddCharacterScreenViewState.isEnableBackHandler: Boolean
    get() = currentStages != CreateStages.NameStage || addCharacterLoadingState == AddCharacterLoadingState.Loading

val AddCharacterScreenViewState.isEnableSegmentProgressBar: Boolean
    get() = addCharacterLoadingState != AddCharacterLoadingState.Loading && addCharacterLoadingState != AddCharacterLoadingState.Success

val AddCharacterScreenViewState.isEnableBackButton: Boolean
    get() = addCharacterLoadingState != AddCharacterLoadingState.Loading && addCharacterLoadingState != AddCharacterLoadingState.Success

val AddCharacterScreenViewState.isShowBottomButtons: Boolean
    get() = addCharacterLoadingState != AddCharacterLoadingState.Loading && addCharacterLoadingState != AddCharacterLoadingState.Success

val AddCharacterScreenViewState.isShowPreviewCard: Boolean
    get() = !characterConfigState.welcomeCardState.welcomeCardDescription.isNullOrEmpty() || !characterConfigState.welcomeCardState.welcomeCardImages.isNullOrEmpty()

val AddCharacterScreenViewState.hasPendingImageUploads: Boolean
    get() = pendingCharacterImageUploads.isNotEmpty() || pendingWelcomeCardImageUploads.isNotEmpty()
