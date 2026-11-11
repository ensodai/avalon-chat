package com.ensodai.avalon.presentation.addCharacterScreen.actions

import com.ensodai.avalon.data.db.dto.CategoryItemDto
import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.data.db.dto.WelcomeCardImageDto
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import com.ensodai.shared.avalon.character.models.InteractionFormat
import com.ensodai.avalon.presentation.commonComponents.welcomCard.CardColor
import com.ensodai.avalon.presentation.core.BaseActions
import io.github.vinceglb.filekit.PlatformFile
import kotlin.uuid.Uuid

data class AddCharacterScreenActions(
    val onChangeName: (String) -> Unit,
    val goToNextStage: () -> Unit,
    val goToPreviousStage: () -> Unit,
    val publishCharacter: () -> Unit,
    val onChangeAvailabilityOption: (CharacterVisibility) -> Unit,
    val onChangeShortDescription: (String) -> Unit,
    val onChangeFullDescription: (String) -> Unit,
    val onChangeScenario: (String) -> Unit,
    val onChangeMesExample: (String) -> Unit,
    val onShowAdvancedStages: () -> Unit,
    val onChangeTemperature: (Float) -> Unit,
    val onAttachCharacterImages: (List<PlatformFile>) -> Unit,
    val onSelectImageAvatar: (CharacterImageDTO?) -> Unit,
    val onDeleteImage: (CharacterImageDTO?) -> Unit,
    val onChangeFullDescriptionPrivacy: (CharacterVisibility) -> Unit,
    val onCategorySelected: (CategoryItemDto) -> Unit,
    val onCloseAlertDialog: () -> Unit,
    val onShowPermissionDialog: () -> Unit,
    val onClosePermissionDialog: () -> Unit,
    val onChangeFirstMessage: (String) -> Unit,
    val onChangeWelcomeCardDescription: (String) -> Unit,
    val onAttachWelcomeCardImages: (List<PlatformFile>) -> Unit,
    val onDeleteWelcomeImage: (WelcomeCardImageDto) -> Unit,
    val onDeletePendingCharacterImage: (String) -> Unit,
    val onDeletePendingWelcomeImage: (String) -> Unit,
    val onChangeWelcomeCardStartTone: (CardColor) -> Unit,
    val onChangeWelcomeCardEndTone: (CardColor) -> Unit,
    val onToggleLight: () -> Unit,
    val onChangeOutreachOption: (Boolean) -> Unit,
    val onOpenVoicePicker: () -> Unit,
    val onCloseVoicePicker: () -> Unit,
    val onSelectVoice: (VoiceModel) -> Unit,
    val onOpenInteractionFormatPicker: () -> Unit,
    val onCloseInteractionFormatPicker: () -> Unit,
    val onChangeInteractionFormat: (InteractionFormat) -> Unit,
    val onOpenAttachLorebook: () -> Unit,
    val onCloseAttachLorebook: () -> Unit,
    val onSelectLorebook: (Uuid) -> Unit,
    val onDetachLorebook: () -> Unit,
    val onOpenCreateLorebook: () -> Unit,
    val onCloseCreateLorebook: () -> Unit,
    val onChangeCreateLorebookName: (String) -> Unit,
    val onChangeCreateLorebookDesc: (String) -> Unit,
    val onToggleCreateLorebookPublic: (Boolean) -> Unit,
    val onCreateLorebookSubmit: () -> Unit,
    val onImportLorebook: (PlatformFile) -> Unit = {}
) : BaseActions()

