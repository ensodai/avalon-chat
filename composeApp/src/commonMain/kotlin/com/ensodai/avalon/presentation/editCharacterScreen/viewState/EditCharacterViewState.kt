package com.ensodai.avalon.presentation.editCharacterScreen.viewState

import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.shared.avalon.character.models.InteractionFormat
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import com.ensodai.avalon.presentation.characterProfileScreen.viewState.CharacterAvatarWithSeed
import com.ensodai.avalon.presentation.core.BaseViewState
import com.ensodai.avalon.presentation.core.LoadingState
import com.ensodai.avalon.utils.EMPTY_STRING
import kotlin.uuid.Uuid

data class EditCharacterViewState(
    val characterName: String = EMPTY_STRING,
    val images: List<CharacterAvatarWithSeed> = emptyList(),
    val shortDescription: String = EMPTY_STRING,
    val fullDescription: String? = null,
    val scenario: String? = null,
    val mesExample: String? = null,
    val temperature: Float = 0.5f,
    val visibility: CharacterVisibility = CharacterVisibility.PUBLIC,
    val firstMessage: String? = null,
    val isOutreachEnabled: Boolean = false,
    val voice: VoiceModel = VoiceModel.ZEPHYR,
    val interactionFormat: InteractionFormat = InteractionFormat.LITERARY_RPG,
    val isShowVoicePicker: Boolean = false,
    val loadingState: LoadingState = LoadingState.Idle,
    val bottomSheet: BottomSheetEditCharacterScreen = BottomSheetEditCharacterScreen.None,
    val characterId: Uuid? = null,
    val currentUpdatingSection: CurrentUpdatingSection = CurrentUpdatingSection.None,
) : BaseViewState()


sealed class CurrentUpdatingSection {
    object None : CurrentUpdatingSection()
    object Name : CurrentUpdatingSection()
    object Description : CurrentUpdatingSection()
    object Scenario : CurrentUpdatingSection()
    object MesExample : CurrentUpdatingSection()
    object FirstMessage : CurrentUpdatingSection()
    object Visibility : CurrentUpdatingSection()
    object Outreach : CurrentUpdatingSection()
    object Voice : CurrentUpdatingSection()
    object InteractionFormat : CurrentUpdatingSection()
}

sealed class BottomSheetEditCharacterScreen {

    object None : BottomSheetEditCharacterScreen()

    object EditNameBottomSheet : BottomSheetEditCharacterScreen()

    object EditDescriptionBottomSheet : BottomSheetEditCharacterScreen()

    object EditScenarioBottomSheet : BottomSheetEditCharacterScreen()

    object EditMesExampleBottomSheet : BottomSheetEditCharacterScreen()

    object EditFirstMessageBottomSheet : BottomSheetEditCharacterScreen()

    object EditVisibilityBottomSheet : BottomSheetEditCharacterScreen()

    object EditOutreachBottomSheet : BottomSheetEditCharacterScreen()

    object EditInteractionFormatBottomSheet : BottomSheetEditCharacterScreen()
}
