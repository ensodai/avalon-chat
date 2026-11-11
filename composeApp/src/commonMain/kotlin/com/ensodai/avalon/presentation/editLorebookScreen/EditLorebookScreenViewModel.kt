package com.ensodai.avalon.presentation.editLorebookScreen

import androidx.lifecycle.viewModelScope
import com.ensodai.shared.avalon.character.models.CharacterLorebookEntryItemDto
import com.ensodai.shared.avalon.character.models.DeleteLorebookEntryRequest
import com.ensodai.shared.avalon.character.models.ForkLorebookRequest
import com.ensodai.shared.avalon.character.models.LorebookEntryPosition
import com.ensodai.shared.avalon.character.models.SaveLorebookEntryRequest
import com.ensodai.shared.avalon.character.models.ToggleLorebookEntryRequest
import com.ensodai.shared.avalon.character.models.UpdateCharacterLorebookRequest
import com.ensodai.shared.avalon.character.models.UpdateLorebookSettingsRequest
import com.ensodai.avalon.domain.lorebook.LorebookManager
import com.ensodai.avalon.domain.useCases.commonUseCase.UploadPickedImageUseCase
import com.ensodai.avalon.domain.useCases.lorebookUseCase.DeleteLorebookEntryUseCase
import com.ensodai.avalon.domain.useCases.lorebookUseCase.ExportStandaloneLorebookUseCase
import com.ensodai.avalon.domain.useCases.lorebookUseCase.ForkLorebookUseCase
import com.ensodai.avalon.domain.useCases.lorebookUseCase.GetLorebookByIdUseCase
import com.ensodai.avalon.domain.useCases.lorebookUseCase.SaveLorebookEntryUseCase
import com.ensodai.avalon.domain.useCases.lorebookUseCase.ToggleLorebookEntryUseCase
import com.ensodai.avalon.domain.useCases.lorebookUseCase.UpdateCharacterLorebookUseCase
import com.ensodai.avalon.domain.useCases.lorebookUseCase.UpdateLorebookSettingsUseCase
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.lorebook_cover_upload_error
import com.ensodai.avalon.generated.resources.lorebook_error_content_empty
import com.ensodai.avalon.generated.resources.lorebook_error_failed_to_delete_entry
import com.ensodai.avalon.generated.resources.lorebook_error_failed_to_fork
import com.ensodai.avalon.generated.resources.lorebook_error_failed_to_load
import com.ensodai.avalon.generated.resources.lorebook_error_failed_to_save_entry
import com.ensodai.avalon.generated.resources.lorebook_error_failed_to_save_settings
import com.ensodai.avalon.generated.resources.lorebook_error_failed_to_toggle_entry
import com.ensodai.avalon.generated.resources.lorebook_error_keys_or_constant
import com.ensodai.avalon.generated.resources.lorebook_error_name_empty
import com.ensodai.avalon.presentation.core.BaseViewModel
import com.ensodai.avalon.presentation.editLorebookScreen.action.EditLorebookScreenActions
import com.ensodai.avalon.presentation.editLorebookScreen.viewState.EditLorebookScreenViewState
import com.ensodai.avalon.utils.IODispatcher
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class EditLorebookArgs(
    val lorebookId: Uuid
)

@OptIn(ExperimentalUuidApi::class)
@KoinViewModel
class EditLorebookScreenViewModel(
    @InjectedParam val args: EditLorebookArgs,
    private val lorebookManager: LorebookManager,
    private val getLorebookByIdUseCase: GetLorebookByIdUseCase,
    private val forkLorebookUseCase: ForkLorebookUseCase,
    private val updateCharacterLorebookUseCase: UpdateCharacterLorebookUseCase,
    private val updateLorebookSettingsUseCase: UpdateLorebookSettingsUseCase,
    private val saveLorebookEntryUseCase: SaveLorebookEntryUseCase,
    private val deleteLorebookEntryUseCase: DeleteLorebookEntryUseCase,
    private val toggleLorebookEntryUseCase: ToggleLorebookEntryUseCase,
    private val uploadPickedImageUseCase: UploadPickedImageUseCase,
    private val exportStandaloneLorebookUseCase: ExportStandaloneLorebookUseCase
) : BaseViewModel<EditLorebookScreenViewState, EditLorebookScreenActions>(
    initialState = EditLorebookScreenViewState(lorebookId = args.lorebookId)
) {

    val lorebookId: Uuid = args.lorebookId

    private var autoSaveJob: Job? = null
    private var savedMessageJob: Job? = null

    override val actions = EditLorebookScreenActions(
        onNameChanged = { name ->
            updateViewState { it.copy(name = name) }
            triggerAutoSave(debounceMs = 600L)
        },
        onDescriptionChanged = { desc ->
            updateViewState { it.copy(description = desc) }
            triggerAutoSave(debounceMs = 600L)
        },
        onScanDepthChanged = { depth ->
            updateViewState { it.copy(scanDepth = depth.coerceIn(1, 100)) }
            triggerAutoSave(debounceMs = 600L)
        },
        onTokenBudgetChanged = { budget ->
            updateViewState { it.copy(tokenBudget = budget) }
            triggerAutoSave(debounceMs = 600L)
        },
        onRecursiveScanningChanged = { recursive ->
            updateViewState { it.copy(recursiveScanning = recursive) }
            triggerAutoSave(debounceMs = 400L)
        },
        onIsPublicChanged = { isPublic ->
            updateViewState { it.copy(isPublic = isPublic) }
            triggerAutoSave(debounceMs = 400L)
        },
        onForkLorebook = {
            forkLorebook()
        },
        onCoverPicked = { file ->
            uploadCover(file)
        },
        onRemoveCover = {
            updateViewState { it.copy(avatarUrl = null) }
            triggerAutoSave(debounceMs = 300L)
        },
        onToggleEntryEnabled = { index ->
            toggleEntryEnabled(index)
        },
        onDeleteEntry = { index ->
            deleteEntry(index)
        },
        onOpenCreateEntry = {
            updateViewState {
                it.copy(
                    isEntryEditorVisible = true,
                    editingEntryId = null,
                    editingComment = "",
                    editingKeys = "",
                    editingSecondaryKeys = "",
                    editingContent = "",
                    editingPosition = LorebookEntryPosition.BEFORE_CHAR,
                    editingInsertionOrder = it.entries.size,
                    editingConstant = false,
                    editingSelective = false,
                    editingUseRegex = false,
                    editingEnabled = true,
                    entryEditorError = null
                )
            }
        },
        onOpenEditEntry = { index ->
            val entry = viewState.value.entries.getOrNull(index)
            if (entry != null) {
                updateViewState {
                    it.copy(
                        isEntryEditorVisible = true,
                        editingEntryId = entry.id,
                        editingComment = entry.comment.orEmpty(),
                        editingKeys = entry.keys.joinToString(", "),
                        editingSecondaryKeys = entry.secondaryKeys?.joinToString(", ").orEmpty(),
                        editingContent = entry.content,
                        editingPosition = entry.position,
                        editingInsertionOrder = entry.insertionOrder,
                        editingConstant = entry.constant,
                        editingSelective = entry.selective,
                        editingUseRegex = entry.useRegex,
                        editingEnabled = entry.enabled,
                        entryEditorError = null
                    )
                }
            }
        },
        onCloseEntryEditor = {
            updateViewState {
                it.copy(
                    isEntryEditorVisible = false,
                    editingEntryId = null,
                    entryEditorError = null
                )
            }
        },
        onEditingCommentChanged = { comment ->
            updateViewState { it.copy(editingComment = comment) }
        },
        onEditingKeysChanged = { keys ->
            updateViewState { it.copy(editingKeys = keys) }
        },
        onEditingSecondaryKeysChanged = { secondaryKeys ->
            updateViewState { it.copy(editingSecondaryKeys = secondaryKeys) }
        },
        onEditingContentChanged = { content ->
            updateViewState { it.copy(editingContent = content) }
        },
        onEditingPositionChanged = { position ->
            updateViewState { it.copy(editingPosition = position) }
        },
        onEditingInsertionOrderChanged = { order ->
            updateViewState { it.copy(editingInsertionOrder = order) }
        },
        onEditingConstantChanged = { constant ->
            updateViewState { it.copy(editingConstant = constant) }
        },
        onEditingSelectiveChanged = { selective ->
            updateViewState { it.copy(editingSelective = selective) }
        },
        onEditingUseRegexChanged = { regex ->
            updateViewState { it.copy(editingUseRegex = regex) }
        },
        onSaveEditingEntry = {
            saveEditingEntry()
        },
        onSaveSettings = {
            saveSettings()
        },
        onSaveLorebook = {
            saveLorebook()
        },
        onExportLorebook = {
            exportLorebook()
        },
        onDismissExportMessage = {
            updateViewState { it.copy(exportSuccessMessage = null) }
        }
    )

    init {
        loadLorebook()
    }

    private fun loadLorebook() {
        viewModelScope.launch {
            updateViewState { it.copy(isLoading = true, errorMessage = null) }
            try {
                val lorebook = getLorebookByIdUseCase.execute(lorebookId)
                if (lorebook != null) {
                    val desc = lorebook.description.orEmpty()
                    updateViewState {
                        it.copy(
                            isLoading = false,
                            name = lorebook.name,
                            avatarUrl = lorebook.avatarUrl,
                            description = desc,
                            scanDepth = lorebook.scanDepth,
                            tokenBudget = lorebook.tokenBudget,
                            recursiveScanning = lorebook.recursiveScanning,
                            isPublic = lorebook.isPublic,
                            isOwner = lorebook.isOwner,
                            initialName = lorebook.name,
                            initialAvatarUrl = lorebook.avatarUrl,
                            initialDescription = desc,
                            initialScanDepth = lorebook.scanDepth,
                            initialTokenBudget = lorebook.tokenBudget,
                            initialRecursiveScanning = lorebook.recursiveScanning,
                            initialIsPublic = lorebook.isPublic,
                            entries = lorebook.entries
                        )
                    }
                } else {
                    val fallback = getString(Res.string.lorebook_error_failed_to_load)
                    updateViewState {
                        it.copy(
                            isLoading = false,
                            errorMessage = fallback
                        )
                    }
                }
            } catch (e: Exception) {
                val fallback = getString(Res.string.lorebook_error_failed_to_load)
                updateViewState {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: fallback
                    )
                }
            }
        }
    }

    private fun toggleEntryEnabled(index: Int) {
        val state = viewState.value
        val entry = state.entries.getOrNull(index) ?: return
        val newEnabled = !entry.enabled

        // 1. Оптимистичное обновление UI
        updateViewState { s ->
            val updated = s.entries.toMutableList()
            if (index in updated.indices) {
                updated[index] = updated[index].copy(enabled = newEnabled)
            }
            s.copy(entries = updated)
        }

        // 2. Фоновый вызов
        viewModelScope.launch {
            try {
                toggleLorebookEntryUseCase.execute(
                    ToggleLorebookEntryRequest(
                        lorebookId = lorebookId,
                        entryId = entry.id,
                        enabled = newEnabled
                    )
                )
            } catch (e: Exception) {
                val fallback = getString(Res.string.lorebook_error_failed_to_toggle_entry)
                // Откат при ошибке
                updateViewState { s ->
                    val reverted = s.entries.toMutableList()
                    val entryIdx = reverted.indexOfFirst { it.id == entry.id }
                    if (entryIdx >= 0) {
                        reverted[entryIdx] = reverted[entryIdx].copy(enabled = entry.enabled)
                    }
                    s.copy(
                        entries = reverted,
                        errorMessage = e.message ?: fallback
                    )
                }
            }
        }
    }

    private fun deleteEntry(index: Int) {
        val state = viewState.value
        val entry = state.entries.getOrNull(index) ?: return

        // 1. Оптимистичное удаление
        updateViewState { s ->
            val updated = s.entries.toMutableList()
            if (index in updated.indices) {
                updated.removeAt(index)
            }
            s.copy(entries = updated)
        }

        // 2. Фоновый вызов
        lorebookManager.notifyEntriesCountChanged(lorebookId, -1)
        viewModelScope.launch {
            try {
                deleteLorebookEntryUseCase.execute(
                    DeleteLorebookEntryRequest(
                        lorebookId = lorebookId,
                        entryId = entry.id
                    )
                )
            } catch (e: Exception) {
                lorebookManager.notifyEntriesCountChanged(lorebookId, +1)
                val fallback = getString(Res.string.lorebook_error_failed_to_delete_entry)
                // Откат при ошибке
                updateViewState { s ->
                    val reverted = s.entries.toMutableList()
                    val insertAt = index.coerceIn(0, reverted.size)
                    reverted.add(insertAt, entry)
                    s.copy(
                        entries = reverted,
                        errorMessage = e.message ?: fallback
                    )
                }
            }
        }
    }

    private fun saveEditingEntry() {
        viewModelScope.launch {
            val state = viewState.value
            val keysList = state.editingKeys
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            val secondaryKeysList = state.editingSecondaryKeys
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .ifEmpty { null }

            if (!state.editingConstant && keysList.isEmpty()) {
                val errorMsg = getString(Res.string.lorebook_error_keys_or_constant)
                updateViewState { it.copy(entryEditorError = errorMsg) }
                return@launch
            }

            if (state.editingContent.isBlank()) {
                val errorMsg = getString(Res.string.lorebook_error_content_empty)
                updateViewState { it.copy(entryEditorError = errorMsg) }
                return@launch
            }

            val entryId = state.editingEntryId ?: Uuid.random()
            val newEntry = CharacterLorebookEntryItemDto(
                id = entryId,
                keys = keysList,
                secondaryKeys = secondaryKeysList,
                content = state.editingContent.trim(),
                comment = state.editingComment.trim().ifBlank { null },
                enabled = state.editingEnabled,
                constant = state.editingConstant,
                selective = state.editingSelective,
                insertionOrder = state.editingInsertionOrder,
                position = state.editingPosition,
                useRegex = state.editingUseRegex
            )

            val previousEntries = state.entries
            val updatedEntries = state.entries.toMutableList()
            val existingIndex = updatedEntries.indexOfFirst { it.id == entryId }
            val isNewEntry = existingIndex < 0
            if (existingIndex >= 0) {
                updatedEntries[existingIndex] = newEntry
            } else {
                updatedEntries.add(newEntry)
            }

            if (isNewEntry) {
                lorebookManager.notifyEntriesCountChanged(lorebookId, +1)
            }

            // 1. Оптимистичное сохранение в стейт и закрытие модалки
            updateViewState {
                it.copy(
                    entries = updatedEntries,
                    isEntryEditorVisible = false,
                    editingEntryId = null,
                    entryEditorError = null
                )
            }

            // 2. Фоновый вызов
            try {
                val saved = saveLorebookEntryUseCase.execute(
                    SaveLorebookEntryRequest(
                        lorebookId = lorebookId,
                        entry = newEntry
                    )
                )
                updateViewState { s ->
                    val list = s.entries.toMutableList()
                    val idx = list.indexOfFirst { it.id == entryId || it.id == saved.id }
                    if (idx >= 0) {
                        list[idx] = saved
                    }
                    s.copy(entries = list)
                }
            } catch (e: Exception) {
                if (isNewEntry) {
                    lorebookManager.notifyEntriesCountChanged(lorebookId, -1)
                }
                val fallback = getString(Res.string.lorebook_error_failed_to_save_entry)
                updateViewState { s ->
                    s.copy(
                        entries = previousEntries,
                        errorMessage = e.message ?: fallback
                    )
                }
            }
        }
    }

    private fun saveSettings() {
        autoSaveJob?.cancel()
        viewModelScope.launch {
            performSaveSettings()
        }
    }

    private fun triggerAutoSave(debounceMs: Long) {
        val state = viewState.value
        if (!state.isOwner || state.name.isBlank()) return

        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            if (debounceMs > 0) {
                delay(debounceMs)
            }
            performSaveSettings()
        }
    }

    private suspend fun performSaveSettings() {
        val state = viewState.value
        if (state.name.isBlank()) return

        updateViewState { it.copy(isSettingsSaving = true, errorMessage = null) }
        try {
            val request = UpdateLorebookSettingsRequest(
                lorebookId = lorebookId,
                name = state.name.trim(),
                description = state.description.trim().ifBlank { null },
                scanDepth = state.scanDepth,
                tokenBudget = state.tokenBudget,
                recursiveScanning = state.recursiveScanning,
                isPublic = state.isPublic,
                avatarUrl = state.avatarUrl
            )
            updateLorebookSettingsUseCase.execute(request)
            lorebookManager.notifyLorebookMetadataUpdated(
                lorebookId = lorebookId,
                name = state.name.trim(),
                description = state.description.trim().ifBlank { null },
                isPublic = state.isPublic,
                avatarUrl = state.avatarUrl
            )
            updateViewState {
                it.copy(
                    isSettingsSaving = false,
                    isSettingsSavedMessageVisible = true,
                    initialName = state.name.trim(),
                    initialAvatarUrl = state.avatarUrl,
                    initialDescription = state.description.trim(),
                    initialScanDepth = state.scanDepth,
                    initialTokenBudget = state.tokenBudget,
                    initialRecursiveScanning = state.recursiveScanning,
                    initialIsPublic = state.isPublic
                )
            }
            savedMessageJob?.cancel()
            savedMessageJob = viewModelScope.launch {
                delay(2000L)
                updateViewState { it.copy(isSettingsSavedMessageVisible = false) }
            }
        } catch (e: Exception) {
            val fallback = getString(Res.string.lorebook_error_failed_to_save_settings)
            updateViewState {
                it.copy(
                    isSettingsSaving = false,
                    errorMessage = e.message ?: fallback
                )
            }
        }
    }

    private fun saveLorebook() {
        viewModelScope.launch {
            val state = viewState.value
            if (state.name.isBlank()) {
                val errorMsg = getString(Res.string.lorebook_error_name_empty)
                updateViewState { it.copy(errorMessage = errorMsg) }
                return@launch
            }

            updateViewState { it.copy(isSaving = true, errorMessage = null) }
            try {
                val request = UpdateCharacterLorebookRequest(
                    lorebookId = lorebookId,
                    name = state.name.trim(),
                    description = state.description.trim().ifBlank { null },
                    scanDepth = state.scanDepth,
                    tokenBudget = state.tokenBudget,
                    recursiveScanning = state.recursiveScanning,
                    isPublic = state.isPublic,
                    avatarUrl = state.avatarUrl,
                    entries = state.entries
                )
                updateCharacterLorebookUseCase.execute(request)
                lorebookManager.notifyLorebookMetadataUpdated(
                    lorebookId = lorebookId,
                    name = state.name.trim(),
                    description = state.description.trim().ifBlank { null },
                    isPublic = state.isPublic,
                    avatarUrl = state.avatarUrl
                )
                updateViewState {
                    it.copy(
                        isSaving = false,
                        saveSuccess = true
                    )
                }
            } catch (e: Exception) {
                val fallback = getString(Res.string.lorebook_error_failed_to_save_settings)
                updateViewState {
                    it.copy(
                        isSaving = false,
                        errorMessage = e.message ?: fallback
                    )
                }
            }
        }
    }

    private fun forkLorebook() {
        viewModelScope.launch {
            updateViewState { it.copy(isLoading = true, errorMessage = null) }
            try {
                val forked = forkLorebookUseCase.execute(ForkLorebookRequest(lorebookId = lorebookId))
                viewModelScope.launch {
                    lorebookManager.loadUserLibrary()
                }
                updateViewState {
                    it.copy(
                        isLoading = false,
                        name = forked.name,
                        avatarUrl = forked.avatarUrl,
                        description = forked.description.orEmpty(),
                        scanDepth = forked.scanDepth,
                        tokenBudget = forked.tokenBudget,
                        recursiveScanning = forked.recursiveScanning,
                        isPublic = forked.isPublic,
                        isOwner = true,
                        initialName = forked.name,
                        initialAvatarUrl = forked.avatarUrl,
                        initialDescription = forked.description.orEmpty(),
                        initialScanDepth = forked.scanDepth,
                        initialTokenBudget = forked.tokenBudget,
                        initialRecursiveScanning = forked.recursiveScanning,
                        initialIsPublic = forked.isPublic,
                        entries = forked.entries
                    )
                }
            } catch (e: Exception) {
                val fallback = getString(Res.string.lorebook_error_failed_to_fork)
                updateViewState {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: fallback
                    )
                }
            }
        }
    }

    private fun uploadCover(file: PlatformFile) {
        viewModelScope.launch {
            updateViewState { it.copy(isUploadingCover = true, errorMessage = null) }
            val result = uploadPickedImageUseCase.uploadLorebookCoverImage(file)
            result.onSuccess { uploaded ->
                updateViewState { it.copy(isUploadingCover = false, avatarUrl = uploaded.imageUrl) }
                triggerAutoSave(debounceMs = 0L)
            }.onFailure { err ->
                val fallback = getString(Res.string.lorebook_cover_upload_error)
                updateViewState {
                    it.copy(
                        isUploadingCover = false,
                        errorMessage = err.message ?: fallback
                    )
                }
            }
        }
    }

    private fun exportLorebook() {
        if (viewState.value.isExporting) return
        viewModelScope.launch(IODispatcher) {
            updateViewState { it.copy(isExporting = true, errorMessage = null, exportSuccessMessage = null) }
            val result = exportStandaloneLorebookUseCase.exportAndSave(lorebookId)
            result.onSuccess { savedPath ->
                updateViewState {
                    it.copy(
                        isExporting = false,
                        exportSuccessMessage = "Книга знаний сохранена: $savedPath"
                    )
                }
            }.onFailure { error ->
                updateViewState {
                    it.copy(
                        isExporting = false,
                        errorMessage = error.message ?: "Ошибка при экспорте книги знаний"
                    )
                }
            }
        }
    }
}
