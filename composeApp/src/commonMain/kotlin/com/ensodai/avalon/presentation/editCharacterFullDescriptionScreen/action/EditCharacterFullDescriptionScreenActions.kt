package com.ensodai.avalon.presentation.editCharacterFullDescriptionScreen.action

import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.avalon.presentation.core.BaseActions

data class EditCharacterFullDescriptionScreenActions(
    val onUpdateCharacterFullDescription: (String) -> Unit,
    val onUpdateFullDescriptionVisibility: (CharacterVisibility) -> Unit
) : BaseActions()