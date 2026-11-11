package com.ensodai.avalon.presentation.editCharacterTextScreen.action

import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.avalon.presentation.core.BaseActions

data class EditCharacterTextScreenActions(
    val onSaveText: (String) -> Unit,
    val onChangeVisibility: (CharacterVisibility) -> Unit
) : BaseActions()
