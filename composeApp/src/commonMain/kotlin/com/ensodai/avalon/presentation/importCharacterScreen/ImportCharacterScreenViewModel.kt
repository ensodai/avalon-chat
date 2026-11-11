package com.ensodai.avalon.presentation.importCharacterScreen

import com.ensodai.avalon.domain.useCases.importCharacterCardUseCase.ImportCharacterCardUseCase
import com.ensodai.avalon.presentation.core.BaseViewModel
import com.ensodai.avalon.presentation.importCharacterScreen.action.ImportCharacterScreenActions
import com.ensodai.avalon.presentation.importCharacterScreen.action.onChangeVisibility
import com.ensodai.avalon.presentation.importCharacterScreen.action.onClearError
import com.ensodai.avalon.presentation.importCharacterScreen.action.onFilePicked
import com.ensodai.avalon.presentation.importCharacterScreen.action.onImportClick
import com.ensodai.avalon.presentation.importCharacterScreen.action.onResetFile
import com.ensodai.avalon.presentation.importCharacterScreen.action.onUpdateCustomization
import com.ensodai.avalon.presentation.importCharacterScreen.viewState.ImportCharacterScreenViewState
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class ImportCharacterScreenViewModel(
    val importCharacterCardUseCase: ImportCharacterCardUseCase
) : BaseViewModel<ImportCharacterScreenViewState, ImportCharacterScreenActions>(
    initialState = ImportCharacterScreenViewState()
) {
    override val actions = ImportCharacterScreenActions(
        onFilePicked = ::onFilePicked,
        onChangeVisibility = ::onChangeVisibility,
        onUpdateCustomization = ::onUpdateCustomization,
        onImportClick = ::onImportClick,
        onClearError = ::onClearError,
        onResetFile = ::onResetFile
    )
}
