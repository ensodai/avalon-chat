@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.ensodai.avalon.presentation.userCharactersScreen.action

import com.ensodai.avalon.data.db.dto.CharacterWithShortDetails
import com.ensodai.avalon.presentation.core.BaseActions
import kotlin.uuid.Uuid

data class UserCharacterScreenActions(
    val onDeleteCharacter: (item: CharacterWithShortDetails) -> Unit,
    val onClaimEnergy: (characterId: Uuid) -> Unit,
    val onClearClaimSuccess: () -> Unit
) : BaseActions()