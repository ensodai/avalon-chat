package com.ensodai.avalon.presentation.editCharacterScreen.action

import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.shared.avalon.character.models.InteractionFormat
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import com.ensodai.avalon.presentation.core.BaseActions

data class EditCharacterActions(
    val bottomSheetActions: BottomSheetActions,
    val bottomSheetEditActions: BottomSheetEditActions
) : BaseActions()

data class BottomSheetActions(
    val onCloseBottomSheet: () -> Unit,
    val onOpenEditNameBottomSheet: () -> Unit,
    val onOpenEditDescriptionBottomSheet: () -> Unit,
    val onOpenEditScenarioBottomSheet: () -> Unit,
    val onOpenEditMesExampleBottomSheet: () -> Unit,
    val onOpenEditFirstMessageBottomSheet: () -> Unit,
    val onOpenEditVisibilityBottomSheet: () -> Unit,
    val onOpenEditOutreachBottomSheet: () -> Unit,
    val onOpenVoicePicker: () -> Unit,
    val onCloseVoicePicker: () -> Unit,
    val onOpenEditInteractionFormatBottomSheet: () -> Unit
)

data class BottomSheetEditActions(
    val onUpdateCharacterName: (String) -> Unit,
    val onUpdateCharacterDescription: (String) -> Unit,
    val onUpdateCharacterScenario: (String?) -> Unit,
    val onUpdateCharacterMesExample: (String?) -> Unit,
    val onUpdateCharacterFirstMessage: (String) -> Unit,
    val onUpdateCharacterVisibility: (CharacterVisibility) -> Unit,
    val onUpdateCharacterOutreach: (Boolean) -> Unit,
    val onUpdateCharacterVoice: (VoiceModel) -> Unit,
    val onUpdateCharacterInteractionFormat: (InteractionFormat) -> Unit
)
