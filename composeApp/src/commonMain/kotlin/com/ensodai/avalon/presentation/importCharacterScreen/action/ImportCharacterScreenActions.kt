package com.ensodai.avalon.presentation.importCharacterScreen.action

import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.shared.avalon.character.spec.AvalonCardCustomization
import com.ensodai.avalon.presentation.core.BaseActions
import io.github.vinceglb.filekit.PlatformFile

data class ImportCharacterScreenActions(
    val onFilePicked: (PlatformFile?) -> Unit = {},
    val onChangeVisibility: (CharacterVisibility) -> Unit = {},
    val onUpdateCustomization: (AvalonCardCustomization) -> Unit = {},
    val onImportClick: () -> Unit = {},
    val onClearError: () -> Unit = {},
    val onResetFile: () -> Unit = {}
) : BaseActions()
