package com.ensodai.avalon.presentation.characterProfileScreen.action

import com.ensodai.shared.avalon.character.models.CharacterCardExportFormat
import com.ensodai.avalon.data.db.cache.AvailableLlmConfigs
import com.ensodai.avalon.presentation.core.BaseActions
import io.github.vinceglb.filekit.PlatformFile

data class CharacterProfileScreenActions(
    val onReportCharacter: () -> Unit,
    val onShowReportDialog: () -> Unit,
    val onCloseReportDialog: () -> Unit,
    val onChangeReportMessage: (String) -> Unit,
    val onUnlockImage: suspend (kotlin.uuid.Uuid) -> Result<Unit> = { Result.success(Unit) },
    val onSelectActiveLlmConfig: (config: AvailableLlmConfigs) -> Unit = {},
    val onExportCharacterCard: (CharacterCardExportFormat) -> Unit = {},
    val onDismissExportMessage: () -> Unit = {},
    val onToggleCardFlip: () -> Unit = {},
    val onSetCardFlipped: (Boolean) -> Unit = {},
    val onAttachUserLorebook: (kotlin.uuid.Uuid) -> Unit = {},
    val onDetachUserLorebook: (kotlin.uuid.Uuid) -> Unit = {},
    val onLoadUserLorebooksLibrary: () -> Unit = {},
    val onImportLorebook: (PlatformFile) -> Unit = {}
) : BaseActions()
