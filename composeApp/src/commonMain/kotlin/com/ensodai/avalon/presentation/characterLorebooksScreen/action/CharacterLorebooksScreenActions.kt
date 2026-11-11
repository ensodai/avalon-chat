package com.ensodai.avalon.presentation.characterLorebooksScreen.action

import com.ensodai.avalon.presentation.core.BaseActions
import io.github.vinceglb.filekit.PlatformFile
import kotlin.uuid.Uuid

data class CharacterLorebooksScreenActions(
    val onToggleBindingEnabled: (lorebookId: Uuid, enabled: Boolean) -> Unit,
    val onSetPrimary: (lorebookId: Uuid) -> Unit,
    val onDetachClick: (lorebookId: Uuid) -> Unit,
    val onOpenAttachSheet: () -> Unit,
    val onCloseAttachSheet: () -> Unit,
    val onAttachFromLibrary: (lorebookId: Uuid) -> Unit,
    val onImportLorebook: (PlatformFile) -> Unit = {},
    val onOpenCreateDialog: () -> Unit,
    val onCloseCreateDialog: () -> Unit,
    val onNewBookNameChanged: (String) -> Unit,
    val onNewBookDescriptionChanged: (String) -> Unit,
    val onNewBookIsPublicChanged: (Boolean) -> Unit,
    val onCreateAndAttachBook: () -> Unit,
    val onOpenLorebookDetails: (lorebookId: Uuid) -> Unit
) : BaseActions()
