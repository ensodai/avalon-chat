package com.ensodai.avalon.presentation.editLorebookScreen.action

import com.ensodai.shared.avalon.character.models.LorebookEntryPosition
import com.ensodai.avalon.presentation.core.BaseActions
import io.github.vinceglb.filekit.PlatformFile

data class EditLorebookScreenActions(
    val onNameChanged: (String) -> Unit,
    val onDescriptionChanged: (String) -> Unit,
    val onScanDepthChanged: (Int) -> Unit,
    val onTokenBudgetChanged: (Int?) -> Unit,
    val onRecursiveScanningChanged: (Boolean) -> Unit,
    val onIsPublicChanged: (Boolean) -> Unit,
    val onForkLorebook: () -> Unit,
    val onCoverPicked: (PlatformFile) -> Unit,
    val onRemoveCover: () -> Unit,

    // Entry list actions
    val onToggleEntryEnabled: (index: Int) -> Unit,
    val onDeleteEntry: (index: Int) -> Unit,

    // Entry editor actions
    val onOpenCreateEntry: () -> Unit,
    val onOpenEditEntry: (index: Int) -> Unit,
    val onCloseEntryEditor: () -> Unit,
    val onEditingCommentChanged: (String) -> Unit,
    val onEditingKeysChanged: (String) -> Unit,
    val onEditingSecondaryKeysChanged: (String) -> Unit,
    val onEditingContentChanged: (String) -> Unit,
    val onEditingPositionChanged: (LorebookEntryPosition) -> Unit,
    val onEditingInsertionOrderChanged: (Int) -> Unit,
    val onEditingConstantChanged: (Boolean) -> Unit,
    val onEditingSelectiveChanged: (Boolean) -> Unit,
    val onEditingUseRegexChanged: (Boolean) -> Unit,
    val onSaveEditingEntry: () -> Unit,

    // Save settings / lorebook
    val onSaveSettings: () -> Unit,
    val onSaveLorebook: () -> Unit,

    // Export standalone lorebook
    val onExportLorebook: () -> Unit = {},
    val onDismissExportMessage: () -> Unit = {}
) : BaseActions()
