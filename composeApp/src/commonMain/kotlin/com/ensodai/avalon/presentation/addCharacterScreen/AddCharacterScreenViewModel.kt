package com.ensodai.avalon.presentation.addCharacterScreen

import com.ensodai.avalon.domain.useCases.addCharacterScreenUseCase.GetCategoriesUseCase
import com.ensodai.avalon.domain.useCases.commonUseCase.CreateNewCharacterUseCase
import com.ensodai.avalon.domain.useCases.commonUseCase.UploadPickedImageUseCase
import com.ensodai.avalon.domain.useCases.lorebookUseCase.CreateStandaloneLorebookUseCase
import com.ensodai.avalon.domain.useCases.lorebookUseCase.GetUserLorebooksLibraryUseCase
import com.ensodai.avalon.domain.useCases.lorebookUseCase.ImportStandaloneLorebookUseCase
import com.ensodai.avalon.presentation.addCharacterScreen.actions.AddCharacterScreenActions
import com.ensodai.avalon.presentation.addCharacterScreen.actions.attachCharacterImages
import com.ensodai.avalon.presentation.addCharacterScreen.actions.attachWelcomeCardImages
import com.ensodai.avalon.presentation.addCharacterScreen.actions.changeAvailabilityOption
import com.ensodai.avalon.presentation.addCharacterScreen.actions.changeFirstMessage
import com.ensodai.avalon.presentation.addCharacterScreen.actions.changeFullDescription
import com.ensodai.avalon.presentation.addCharacterScreen.actions.changeMesExample
import com.ensodai.avalon.presentation.addCharacterScreen.actions.changeName
import com.ensodai.avalon.presentation.addCharacterScreen.actions.changeScenario
import com.ensodai.avalon.presentation.addCharacterScreen.actions.changeShortDescription
import com.ensodai.avalon.presentation.addCharacterScreen.actions.changeTemperature
import com.ensodai.avalon.presentation.addCharacterScreen.actions.changeOutreachOption
import com.ensodai.avalon.presentation.addCharacterScreen.actions.openVoicePicker
import com.ensodai.avalon.presentation.addCharacterScreen.actions.closeVoicePicker
import com.ensodai.avalon.presentation.addCharacterScreen.actions.selectVoice
import com.ensodai.avalon.presentation.addCharacterScreen.actions.openInteractionFormatPicker
import com.ensodai.avalon.presentation.addCharacterScreen.actions.closeInteractionFormatPicker
import com.ensodai.avalon.presentation.addCharacterScreen.actions.changeInteractionFormat
import com.ensodai.avalon.presentation.addCharacterScreen.actions.openAttachLorebook
import com.ensodai.avalon.presentation.addCharacterScreen.actions.closeAttachLorebook
import com.ensodai.avalon.presentation.addCharacterScreen.actions.selectLorebook
import com.ensodai.avalon.presentation.addCharacterScreen.actions.detachLorebook
import com.ensodai.avalon.presentation.addCharacterScreen.actions.openCreateLorebook
import com.ensodai.avalon.presentation.addCharacterScreen.actions.closeCreateLorebook
import com.ensodai.avalon.presentation.addCharacterScreen.actions.changeCreateLorebookName
import com.ensodai.avalon.presentation.addCharacterScreen.actions.changeCreateLorebookDesc
import com.ensodai.avalon.presentation.addCharacterScreen.actions.toggleCreateLorebookPublic
import com.ensodai.avalon.presentation.addCharacterScreen.actions.createLorebookSubmit
import com.ensodai.avalon.presentation.addCharacterScreen.actions.importLorebook
import com.ensodai.avalon.presentation.addCharacterScreen.actions.changeWelcomeCardDescription
import com.ensodai.avalon.presentation.addCharacterScreen.actions.changeWelcomeCardEndTone
import com.ensodai.avalon.presentation.addCharacterScreen.actions.changeWelcomeCardStartTone
import com.ensodai.avalon.presentation.addCharacterScreen.actions.createViewState
import com.ensodai.avalon.presentation.addCharacterScreen.actions.deleteImage
import com.ensodai.avalon.presentation.addCharacterScreen.actions.deletePendingCharacterImage
import com.ensodai.avalon.presentation.addCharacterScreen.actions.deletePendingWelcomeImage
import com.ensodai.avalon.presentation.addCharacterScreen.actions.deleteWelcomeImage
import com.ensodai.avalon.presentation.addCharacterScreen.actions.goToNextStage
import com.ensodai.avalon.presentation.addCharacterScreen.actions.goToPreviousStage
import com.ensodai.avalon.presentation.addCharacterScreen.actions.onCategorySelected
import com.ensodai.avalon.presentation.addCharacterScreen.actions.onChangeFullDescriptionPrivacy
import com.ensodai.avalon.presentation.addCharacterScreen.actions.onCloseAlertDialog
import com.ensodai.avalon.presentation.addCharacterScreen.actions.onClosePermissionDialog
import com.ensodai.avalon.presentation.addCharacterScreen.actions.onShowPermissionDialog
import com.ensodai.avalon.presentation.addCharacterScreen.actions.onToggleLight
import com.ensodai.avalon.presentation.addCharacterScreen.actions.publishCharacter
import com.ensodai.avalon.presentation.addCharacterScreen.actions.selectImageAvatar
import com.ensodai.avalon.presentation.addCharacterScreen.actions.showAdvancedStages
import com.ensodai.avalon.presentation.addCharacterScreen.viewState.AddCharacterScreenViewState
import com.ensodai.avalon.presentation.core.BaseViewModel
import kotlinx.coroutines.Job
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class AddCharacterScreenViewModel(
    private val saveConfigUseCase: CreateNewCharacterUseCase,
    private val uploadPickedImageUseCase: UploadPickedImageUseCase,
    private val getCategories: GetCategoriesUseCase,
    private val getUserLorebooksLibraryUseCase: GetUserLorebooksLibraryUseCase,
    private val createStandaloneLorebookUseCase: CreateStandaloneLorebookUseCase,
    private val importStandaloneLorebookUseCase: ImportStandaloneLorebookUseCase
) : BaseViewModel<AddCharacterScreenViewState, AddCharacterScreenActions>(
    initialState = AddCharacterScreenViewState()
) {

    private val characterUploadJobs = mutableMapOf<String, Job>()
    private val welcomeCardUploadJobs = mutableMapOf<String, Job>()

    override val actions = AddCharacterScreenActions(
        onChangeName = { changeName(it) },
        goToNextStage = { goToNextStage() },
        goToPreviousStage = { goToPreviousStage() },
        publishCharacter = { publishCharacter(saveConfigUseCase) },
        onChangeAvailabilityOption = { changeAvailabilityOption(it) },
        onChangeShortDescription = { changeShortDescription(it) },
        onShowAdvancedStages = { showAdvancedStages() },
        onChangeFullDescription = { changeFullDescription(it) },
        onChangeScenario = { changeScenario(it) },
        onChangeMesExample = { changeMesExample(it) },
        onChangeTemperature = { changeTemperature(it) },
        onAttachCharacterImages = { attachCharacterImages(it, uploadPickedImageUseCase) },
        onSelectImageAvatar = { selectImageAvatar(it) },
        onDeleteImage = { deleteImage(it) },
        onChangeFullDescriptionPrivacy = { onChangeFullDescriptionPrivacy(it) },
        onCategorySelected = { onCategorySelected(it) },
        onCloseAlertDialog = { onCloseAlertDialog() },
        onShowPermissionDialog = { onShowPermissionDialog() },
        onClosePermissionDialog = { onClosePermissionDialog() },
        onChangeFirstMessage = { changeFirstMessage(it) },
        onChangeWelcomeCardDescription = { changeWelcomeCardDescription(it) },
        onAttachWelcomeCardImages = { attachWelcomeCardImages(it, uploadPickedImageUseCase) },
        onDeleteWelcomeImage = { deleteWelcomeImage(it) },
        onDeletePendingCharacterImage = { deletePendingCharacterImage(it) },
        onDeletePendingWelcomeImage = { deletePendingWelcomeImage(it) },
        onChangeWelcomeCardStartTone = { changeWelcomeCardStartTone(it) },
        onChangeWelcomeCardEndTone = { changeWelcomeCardEndTone(it) },
        onToggleLight = { onToggleLight() },
        onChangeOutreachOption = { changeOutreachOption(it) },
        onOpenVoicePicker = { openVoicePicker() },
        onCloseVoicePicker = { closeVoicePicker() },
        onSelectVoice = { selectVoice(it) },
        onOpenInteractionFormatPicker = { openInteractionFormatPicker() },
        onCloseInteractionFormatPicker = { closeInteractionFormatPicker() },
        onChangeInteractionFormat = { changeInteractionFormat(it) },
        onOpenAttachLorebook = { openAttachLorebook(getUserLorebooksLibraryUseCase) },
        onCloseAttachLorebook = { closeAttachLorebook() },
        onSelectLorebook = { selectLorebook(it) },
        onDetachLorebook = { detachLorebook() },
        onOpenCreateLorebook = { openCreateLorebook() },
        onCloseCreateLorebook = { closeCreateLorebook() },
        onChangeCreateLorebookName = { changeCreateLorebookName(it) },
        onChangeCreateLorebookDesc = { changeCreateLorebookDesc(it) },
        onToggleCreateLorebookPublic = { toggleCreateLorebookPublic(it) },
        onCreateLorebookSubmit = { createLorebookSubmit(createStandaloneLorebookUseCase) },
        onImportLorebook = { importLorebook(importStandaloneLorebookUseCase, it) }
    )

    init {
        createViewState(
            getCategories = getCategories
        )
    }

    fun registerCharacterUploadJob(pendingId: String, job: Job) {
        characterUploadJobs[pendingId] = job
    }

    fun registerWelcomeCardUploadJob(pendingId: String, job: Job) {
        welcomeCardUploadJobs[pendingId] = job
    }

    fun clearCharacterUploadJob(pendingId: String) {
        characterUploadJobs.remove(pendingId)
    }

    fun clearWelcomeCardUploadJob(pendingId: String) {
        welcomeCardUploadJobs.remove(pendingId)
    }

    fun cancelCharacterUploadJob(pendingId: String) {
        characterUploadJobs.remove(pendingId)?.cancel()
    }

    fun cancelWelcomeCardUploadJob(pendingId: String) {
        welcomeCardUploadJobs.remove(pendingId)?.cancel()
    }

    override fun onCleared() {
        characterUploadJobs.values.forEach { it.cancel() }
        welcomeCardUploadJobs.values.forEach { it.cancel() }
        characterUploadJobs.clear()
        welcomeCardUploadJobs.clear()
        super.onCleared()
    }
}
