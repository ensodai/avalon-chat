package com.ensodai.avalon.presentation.editLorebookScreen.viewState

import com.ensodai.shared.avalon.character.models.CharacterLorebookEntryItemDto
import com.ensodai.shared.avalon.character.models.LorebookEntryPosition
import com.ensodai.avalon.presentation.core.BaseViewState
import kotlin.uuid.Uuid

data class EditLorebookScreenViewState(
    val lorebookId: Uuid,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
    val isExporting: Boolean = false,
    val exportSuccessMessage: String? = null,

    // Lorebook general metadata
    val name: String = "",
    val description: String = "",
    val scanDepth: Int = 5,
    val tokenBudget: Int? = null,
    val recursiveScanning: Boolean = false,
    val isSettingsSaving: Boolean = false,
    val isSettingsSavedMessageVisible: Boolean = false,
    val isCreatingBook: Boolean = false,
    val isPublic: Boolean = false,
    val isOwner: Boolean = true,
    val avatarUrl: String? = null,
    val isUploadingCover: Boolean = false,
    val initialName: String = "",
    val initialDescription: String = "",
    val initialScanDepth: Int = 5,
    val initialTokenBudget: Int? = null,
    val initialRecursiveScanning: Boolean = false,
    val initialIsPublic: Boolean = false,
    val initialAvatarUrl: String? = null,

    // Entries list
    val entries: List<CharacterLorebookEntryItemDto> = emptyList(),

    // Entry editor bottom sheet
    val isEntryEditorVisible: Boolean = false,
    val editingEntryId: Uuid? = null,
    val editingComment: String = "",
    val editingKeys: String = "",
    val editingSecondaryKeys: String = "",
    val editingContent: String = "",
    val editingPosition: LorebookEntryPosition = LorebookEntryPosition.BEFORE_CHAR,
    val editingInsertionOrder: Int = 0,
    val editingConstant: Boolean = false,
    val editingSelective: Boolean = false,
    val editingUseRegex: Boolean = false,
    val editingEnabled: Boolean = true,
    val entryEditorError: String? = null
) : BaseViewState() {
    val isSettingsDirty: Boolean
        get() = name != initialName ||
                description != initialDescription ||
                scanDepth != initialScanDepth ||
                tokenBudget != initialTokenBudget ||
                recursiveScanning != initialRecursiveScanning ||
                isPublic != initialIsPublic ||
                avatarUrl != initialAvatarUrl
}
