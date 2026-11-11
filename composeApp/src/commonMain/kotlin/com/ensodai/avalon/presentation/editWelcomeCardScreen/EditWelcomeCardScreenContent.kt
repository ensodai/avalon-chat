package com.ensodai.avalon.presentation.editWelcomeCardScreen

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.navigation.AvalonBackHandler
import com.ensodai.avalon.presentation.addCharacterScreen.component.helloCardStageScreen.AddCharacterHelloCardStageScreen
import com.ensodai.avalon.presentation.commonComponents.alertDialogs.AlertDialogPermissionImage
import com.ensodai.avalon.presentation.core.LoadingState
import com.ensodai.avalon.presentation.editTemperatureScreen.component.BottomBarEditWelcomeCardScreen
import com.ensodai.avalon.presentation.editWelcomeCardScreen.action.EditWelcomeCardScreenActions
import com.ensodai.avalon.presentation.editWelcomeCardScreen.component.EditWelcomeCardScreenTopBar
import com.ensodai.avalon.presentation.editWelcomeCardScreen.viewState.EditWelcomeCardScreenViewState
import com.ensodai.avalon.presentation.editWelcomeCardScreen.viewState.isShowPreviewCard
import com.ensodai.avalon.presentation.navigation.Navigation

@Composable
fun EditWelcomeCardScreenContent(
    viewState: EditWelcomeCardScreenViewState,
    actions: EditWelcomeCardScreenActions,
    navigation: Navigation,
    onEnableSwipeable: () -> Unit,
    onDisableSwipeable: () -> Unit
) {

    AlertDialogPermissionImage(
        isShowPermissionDialog = viewState.isShowPermissionDialog,
        onCloseButtonClick = { actions.onShowPermissionDialog(false) }
    )

    LaunchedEffect(viewState.loadingState) {
        when (viewState.loadingState) {
            LoadingState.Error -> {
                onEnableSwipeable()
            }

            LoadingState.Idle -> {}
            LoadingState.Loading -> {
                onDisableSwipeable()
            }

            LoadingState.Success -> {
                onEnableSwipeable()
                navigation.navigateBack()
            }
        }
    }

    AvalonBackHandler(viewState.loadingState == LoadingState.Loading) {
        if (viewState.loadingState != LoadingState.Loading) {
            navigation.navigateBack()
        }
    }

    Scaffold(
        topBar = {
            EditWelcomeCardScreenTopBar(
                modifier = Modifier
                    .fillMaxWidth(),
                onBackButtonClick = {
                    navigation.navigateBack()
                }
            )
        },
        bottomBar = {
            BottomBarEditWelcomeCardScreen(
                loadingState = viewState.loadingState,
                isSaveEnabled = viewState.pendingImageUploads.isEmpty(),
                pendingUploadsMessage = if (viewState.pendingImageUploads.isNotEmpty()) "Загружаем изображения..." else null,
                onNavigateBack = { navigation.navigateBack() },
                onSaveClick = { actions.onSaveChanges() }
            )
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier,
            contentPadding = innerPadding
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                AddCharacterHelloCardStageScreen(
                    text = viewState.welcomeCardDescription,
                    images = viewState.welcomeCardImages,
                    pendingUploads = viewState.pendingImageUploads,
                    startColor = viewState.startColor,
                    endColor = viewState.endColor,
                    isShowPreviewCard = viewState.isShowPreviewCard,
                    allowedColors = viewState.allowedColors,
                    isEnableLight = viewState.isEnableLight,
                    onChangeDescription = { actions.onChangeDescription(it) },
                    onAttachImages = { actions.onAttachImages(it) },
                    onShowPermissionDialog = { actions.onShowPermissionDialog(true) },
                    onDeleteImage = { actions.onDeleteImage(it) },
                    onDeletePendingImage = { actions.onDeletePendingImage(it) },
                    onSelectStartTone = { actions.onSelectStartTone(it) },
                    onSelectEndTone = { actions.onSelectEndTone(it) },
                    onToggleLight = { actions.onToggleLight() }
                )
            }
        }
    }
}
