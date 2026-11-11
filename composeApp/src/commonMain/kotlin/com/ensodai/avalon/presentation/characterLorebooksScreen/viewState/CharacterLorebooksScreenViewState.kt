package com.ensodai.avalon.presentation.characterLorebooksScreen.viewState

import com.ensodai.shared.avalon.character.models.CharacterLorebookBindingDto
import com.ensodai.shared.avalon.character.models.UserLorebookSummaryDto
import com.ensodai.avalon.presentation.core.BaseViewState
import kotlin.uuid.Uuid

data class CharacterLorebooksScreenViewState(
    val characterId: Uuid? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val bindings: List<CharacterLorebookBindingDto> = emptyList(),

    // Attach from library sheet state
    val isAttachSheetVisible: Boolean = false,
    val isLibraryLoading: Boolean = false,
    val userLibrary: List<UserLorebookSummaryDto> = emptyList(),

    // Create new book dialog state
    val isCreateDialogVisible: Boolean = false,
    val isCreatingBook: Boolean = false,
    val newBookName: String = "",
    val newBookDescription: String = "",
    val newBookIsPublic: Boolean = false
) : BaseViewState()
