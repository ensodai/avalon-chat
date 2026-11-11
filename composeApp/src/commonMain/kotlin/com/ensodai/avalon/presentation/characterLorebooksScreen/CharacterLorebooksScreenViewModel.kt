@file:OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)

package com.ensodai.avalon.presentation.characterLorebooksScreen

import androidx.lifecycle.viewModelScope
import com.ensodai.avalon.domain.lorebook.LorebookManager
import com.ensodai.avalon.domain.useCases.lorebookUseCase.ImportStandaloneLorebookUseCase
import com.ensodai.avalon.presentation.characterLorebooksScreen.action.CharacterLorebooksScreenActions
import com.ensodai.avalon.presentation.characterLorebooksScreen.viewState.CharacterLorebooksScreenViewState
import com.ensodai.avalon.presentation.core.BaseViewModel
import com.ensodai.avalon.utils.IODispatcher
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.io.encoding.Base64
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@KoinViewModel
class CharacterLorebooksScreenViewModel(
    @InjectedParam val characterId: Uuid,
    private val lorebookManager: LorebookManager,
    private val importStandaloneLorebookUseCase: ImportStandaloneLorebookUseCase
) : BaseViewModel<CharacterLorebooksScreenViewState, CharacterLorebooksScreenActions>(
    initialState = CharacterLorebooksScreenViewState(characterId = characterId)
) {

    override val actions = CharacterLorebooksScreenActions(
        onToggleBindingEnabled = { lorebookId, enabled ->
            toggleBinding(lorebookId, enabled)
        },
        onSetPrimary = { lorebookId ->
            setPrimary(lorebookId)
        },
        onDetachClick = { lorebookId ->
            detachLorebook(lorebookId)
        },
        onOpenAttachSheet = {
            updateViewState { it.copy(isAttachSheetVisible = true) }
            loadUserLibrary()
        },
        onCloseAttachSheet = {
            updateViewState { it.copy(isAttachSheetVisible = false) }
        },
        onAttachFromLibrary = { lorebookId ->
            attachFromLibrary(lorebookId)
        },
        onImportLorebook = { file ->
            importAndAttachBook(file)
        },
        onOpenCreateDialog = {
            updateViewState {
                it.copy(
                    isCreateDialogVisible = true,
                    newBookName = "",
                    newBookDescription = "",
                    newBookIsPublic = false
                )
            }
        },
        onCloseCreateDialog = {
            updateViewState { it.copy(isCreateDialogVisible = false) }
        },
        onNewBookNameChanged = { name ->
            updateViewState { it.copy(newBookName = name) }
        },
        onNewBookDescriptionChanged = { desc ->
            updateViewState { it.copy(newBookDescription = desc) }
        },
        onNewBookIsPublicChanged = { isPublic ->
            updateViewState { it.copy(newBookIsPublic = isPublic) }
        },
        onCreateAndAttachBook = {
            createAndAttachBook()
        },
        onOpenLorebookDetails = { /* Wired in screen composable */ }
    )

    init {
        // Реактивная подписка на привязки текущего персонажа
        viewModelScope.launch {
            lorebookManager.characterBindings.collect { bindings ->
                updateViewState { it.copy(bindings = bindings) }
            }
        }

        // Реактивная подписка на библиотеку
        viewModelScope.launch {
            lorebookManager.userLibrary.collect { library ->
                updateViewState { it.copy(userLibrary = library) }
            }
        }

        loadBindings()
    }

    fun loadBindings() {
        viewModelScope.launch {
            updateViewState { it.copy(isLoading = true, errorMessage = null) }
            try {
                lorebookManager.loadCharacterLorebooks(characterId)
                updateViewState { it.copy(isLoading = false) }
            } catch (e: Exception) {
                updateViewState {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load character lorebooks"
                    )
                }
            }
        }
    }

    private fun toggleBinding(lorebookId: Uuid, enabled: Boolean) {
        viewModelScope.launch {
            try {
                lorebookManager.toggleBinding(characterId, lorebookId, enabled)
            } catch (e: Exception) {
                updateViewState {
                    it.copy(errorMessage = e.message ?: "Failed to update binding")
                }
            }
        }
    }

    private fun setPrimary(lorebookId: Uuid) {
        viewModelScope.launch {
            try {
                lorebookManager.setPrimary(characterId, lorebookId)
            } catch (e: Exception) {
                updateViewState {
                    it.copy(errorMessage = e.message ?: "Failed to set primary lorebook")
                }
            }
        }
    }

    private fun detachLorebook(lorebookId: Uuid) {
        viewModelScope.launch {
            try {
                lorebookManager.detachLorebook(characterId, lorebookId)
            } catch (e: Exception) {
                updateViewState {
                    it.copy(errorMessage = e.message ?: "Failed to detach lorebook")
                }
            }
        }
    }

    private fun loadUserLibrary() {
        viewModelScope.launch {
            updateViewState { it.copy(isLibraryLoading = true) }
            try {
                lorebookManager.loadUserLibrary()
                updateViewState { it.copy(isLibraryLoading = false) }
            } catch (e: Exception) {
                updateViewState {
                    it.copy(
                        isLibraryLoading = false,
                        errorMessage = e.message ?: "Failed to load library"
                    )
                }
            }
        }
    }

    private fun attachFromLibrary(lorebookId: Uuid) {
        viewModelScope.launch {
            try {
                lorebookManager.attachFromLibrary(characterId, lorebookId)
                updateViewState { it.copy(isAttachSheetVisible = false) }
            } catch (e: Exception) {
                updateViewState {
                    it.copy(errorMessage = e.message ?: "Failed to attach lorebook")
                }
            }
        }
    }

    private fun createAndAttachBook() {
        val state = viewState.value
        val name = state.newBookName.trim()
        if (name.isEmpty()) return

        viewModelScope.launch {
            updateViewState { it.copy(isCreatingBook = true, errorMessage = null) }
            try {
                lorebookManager.createAndAttachBook(
                    characterId = characterId,
                    name = name,
                    description = state.newBookDescription.trim().ifBlank { null },
                    isPublic = state.newBookIsPublic
                )
                updateViewState {
                    it.copy(
                        isCreatingBook = false,
                        isCreateDialogVisible = false
                    )
                }
            } catch (e: Exception) {
                updateViewState {
                    it.copy(
                        isCreatingBook = false,
                        errorMessage = e.message ?: "Failed to create lorebook"
                    )
                }
            }
        }
    }

    private fun importAndAttachBook(file: PlatformFile) {
        viewModelScope.launch(IODispatcher) {
            updateViewState { it.copy(isLibraryLoading = true, errorMessage = null) }
            runCatching {
                val bytes = file.readBytes()
                val base64 = Base64.Default.encode(bytes)
                importStandaloneLorebookUseCase.execute(
                    fileBase64 = base64,
                    attachToCharacterId = characterId,
                    isPublic = false
                ).getOrThrow()
            }.onSuccess {
                lorebookManager.loadUserLibrary()
                lorebookManager.loadCharacterLorebooks(characterId)
                updateViewState { it.copy(isLibraryLoading = false, isAttachSheetVisible = false) }
            }.onFailure { error ->
                updateViewState {
                    it.copy(
                        isLibraryLoading = false,
                        errorMessage = error.message ?: "Failed to import lorebook"
                    )
                }
            }
        }
    }
}
