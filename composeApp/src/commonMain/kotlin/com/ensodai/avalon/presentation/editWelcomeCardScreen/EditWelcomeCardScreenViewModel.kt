package com.ensodai.avalon.presentation.editWelcomeCardScreen

import com.ensodai.avalon.domain.useCases.commonUseCase.UploadPickedImageUseCase
import com.ensodai.avalon.domain.useCases.editWelcomeCardUseCase.GetCharacterWelcomeCardUseCase
import com.ensodai.avalon.domain.useCases.editWelcomeCardUseCase.UpdateWelcomeCardUseCase
import com.ensodai.avalon.presentation.core.BaseViewModel
import com.ensodai.avalon.presentation.editWelcomeCardScreen.action.EditWelcomeCardScreenActions
import com.ensodai.avalon.presentation.editWelcomeCardScreen.action.createViewState
import com.ensodai.avalon.presentation.editWelcomeCardScreen.action.onAttachImages
import com.ensodai.avalon.presentation.editWelcomeCardScreen.action.onChangeDescription
import com.ensodai.avalon.presentation.editWelcomeCardScreen.action.onDeleteImage
import com.ensodai.avalon.presentation.editWelcomeCardScreen.action.onDeletePendingImage
import com.ensodai.avalon.presentation.editWelcomeCardScreen.action.onSaveChanges
import com.ensodai.avalon.presentation.editWelcomeCardScreen.action.onSelectEndTone
import com.ensodai.avalon.presentation.editWelcomeCardScreen.action.onSelectStartTone
import com.ensodai.avalon.presentation.editWelcomeCardScreen.action.onShowPermissionDialog
import com.ensodai.avalon.presentation.editWelcomeCardScreen.action.onToggleLight
import com.ensodai.avalon.presentation.editWelcomeCardScreen.viewState.EditWelcomeCardScreenViewState
import kotlinx.coroutines.Job
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
class EditWelcomeCardScreenViewModel(
    @InjectedParam val characterId: Uuid,
    private val getCharacterWelcomeCard: GetCharacterWelcomeCardUseCase,
    private val updateWelcomeCard: UpdateWelcomeCardUseCase,
    private val uploadPickedImageUseCase: UploadPickedImageUseCase
) : BaseViewModel<EditWelcomeCardScreenViewState, EditWelcomeCardScreenActions>(
    initialState = EditWelcomeCardScreenViewState()
) {

    private val uploadJobs = mutableMapOf<String, Job>()

    override val actions = EditWelcomeCardScreenActions(
        onChangeDescription = { onChangeDescription(it) },
        onAttachImages = { onAttachImages(it, uploadPickedImageUseCase) },
        onShowPermissionDialog = { onShowPermissionDialog(true) },
        onDeleteImage = { onDeleteImage(it) },
        onDeletePendingImage = { onDeletePendingImage(it) },
        onSelectStartTone = { onSelectStartTone(it) },
        onSelectEndTone = { onSelectEndTone(it) },
        onToggleLight = { onToggleLight() },
        onSaveChanges = {
            onSaveChanges(
                characterId = characterId,
                updateWelcomeCard = updateWelcomeCard
            )
        }
    )

    init {
        createViewState(
            characterId = characterId,
            getCharacterWelcomeCard = getCharacterWelcomeCard
        )
    }

    fun registerUploadJob(pendingId: String, job: Job) {
        uploadJobs[pendingId] = job
    }

    fun clearUploadJob(pendingId: String) {
        uploadJobs.remove(pendingId)
    }

    fun cancelUploadJob(pendingId: String) {
        uploadJobs.remove(pendingId)?.cancel()
    }

    override fun onCleared() {
        uploadJobs.values.forEach { it.cancel() }
        uploadJobs.clear()
        super.onCleared()
    }

}
