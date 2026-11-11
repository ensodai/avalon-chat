package com.ensodai.avalon.presentation.lorebookDetailsScreen

import androidx.lifecycle.viewModelScope
import com.ensodai.shared.avalon.character.models.ForkLorebookRequest
import com.ensodai.shared.avalon.character.models.ReportLorebookRequest
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import com.ensodai.avalon.domain.lorebook.LorebookManager
import com.ensodai.avalon.presentation.core.BaseViewModel
import com.ensodai.avalon.presentation.lorebookDetailsScreen.action.LorebookDetailsScreenActions
import com.ensodai.avalon.presentation.lorebookDetailsScreen.viewState.LorebookDetailsScreenViewState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@KoinViewModel
class LorebookDetailsViewModel(
    @InjectedParam val lorebookId: Uuid,
    private val characterRpcService: CharacterRpcService,
    private val lorebookManager: LorebookManager
) : BaseViewModel<LorebookDetailsScreenViewState, LorebookDetailsScreenActions>(
    initialState = LorebookDetailsScreenViewState(lorebookId = lorebookId)
) {
    override val actions = LorebookDetailsScreenActions(
        onOpenComplaintDialog = {
            updateViewState { it.copy(isShowComplaintDialog = true, complaintText = "") }
        },
        onCloseComplaintDialog = {
            updateViewState { it.copy(isShowComplaintDialog = false) }
        },
        onComplaintTextChanged = { text ->
            updateViewState { it.copy(complaintText = text) }
        },
        onSubmitComplaint = {
            submitComplaint()
        },
        onToggleSaveToLibrary = {
            toggleSaveToLibrary()
        },
        onForkBook = {
            forkBook()
        },
        onDismissFeedback = {
            updateViewState { it.copy(feedbackMessage = null) }
        }
    )

    init {
        loadLorebook()
        observeLibrary()
    }

    private fun loadLorebook() {
        viewModelScope.launch {
            updateViewState { it.copy(isLoading = true) }
            try {
                val details = characterRpcService.getLorebookById(lorebookId)
                val isOwner = details?.isOwner ?: false
                updateViewState {
                    it.copy(
                        lorebook = details,
                        isOwner = isOwner,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                updateViewState {
                    it.copy(
                        isLoading = false,
                        feedbackMessage = e.message ?: "Failed to load lorebook"
                    )
                }
            }
        }
    }

    private fun observeLibrary() {
        viewModelScope.launch {
            lorebookManager.userLibrary.collectLatest { library ->
                val isInLibrary = library.any { it.id == lorebookId }
                updateViewState { it.copy(isSavedToLibrary = isInLibrary) }
            }
        }
    }

    private fun submitComplaint() {
        val text = viewState.value.complaintText.trim()
        if (text.isBlank()) return

        viewModelScope.launch {
            updateViewState { it.copy(isSubmittingReport = true) }
            try {
                val response = characterRpcService.reportLorebook(
                    ReportLorebookRequest(
                        lorebookId = lorebookId,
                        message = text
                    )
                )
                updateViewState {
                    it.copy(
                        isSubmittingReport = false,
                        isShowComplaintDialog = false,
                        feedbackMessage = response.message
                    )
                }
            } catch (e: Exception) {
                updateViewState {
                    it.copy(
                        isSubmittingReport = false,
                        feedbackMessage = e.message ?: "Failed to submit report"
                    )
                }
            }
        }
    }

    private fun toggleSaveToLibrary() {
        val currentlySaved = viewState.value.isSavedToLibrary
        viewModelScope.launch {
            try {
                if (currentlySaved) {
                    lorebookManager.removeLorebookFromLibrary(lorebookId)
                    updateViewState {
                        it.copy(
                            isSavedToLibrary = false,
                            feedbackMessage = "Книга удалена из библиотеки"
                        )
                    }
                } else {
                    lorebookManager.saveLorebookToLibrary(lorebookId)
                    updateViewState {
                        it.copy(
                            isSavedToLibrary = true,
                            feedbackMessage = "Книга добавлена в вашу библиотеку"
                        )
                    }
                }
            } catch (e: Exception) {
                updateViewState {
                    it.copy(feedbackMessage = e.message ?: "Action failed")
                }
            }
        }
    }

    private fun forkBook() {
        viewModelScope.launch {
            updateViewState { it.copy(isForking = true) }
            try {
                characterRpcService.forkLorebook(
                    ForkLorebookRequest(lorebookId = lorebookId)
                )
                lorebookManager.loadUserLibrary()
                updateViewState {
                    it.copy(
                        isForking = false,
                        feedbackMessage = "Копия книги успешно создана в вашей библиотеке!"
                    )
                }
            } catch (e: Exception) {
                updateViewState {
                    it.copy(
                        isForking = false,
                        feedbackMessage = e.message ?: "Failed to fork lorebook"
                    )
                }
            }
        }
    }
}
