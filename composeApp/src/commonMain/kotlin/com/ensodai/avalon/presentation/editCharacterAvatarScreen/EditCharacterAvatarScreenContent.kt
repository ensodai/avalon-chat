package com.ensodai.avalon.presentation.editCharacterAvatarScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.cancel
import com.ensodai.avalon.generated.resources.save
import com.ensodai.avalon.navigation.AvalonBackHandler
import com.ensodai.avalon.navigation.avalonCustomSwipe
import com.ensodai.avalon.presentation.addCharacterScreen.component.avatarStageScreen.AddCharacterAvatarStageScreen
import com.ensodai.avalon.presentation.commonComponents.alertDialogs.AlertDialogPermissionImage
import com.ensodai.avalon.presentation.commonComponents.buttons.TechnoButton
import com.ensodai.avalon.presentation.core.LoadingState
import com.ensodai.avalon.presentation.editCharacterAvatarScreen.action.EditCharacterAvatarScreenActions
import com.ensodai.avalon.presentation.editCharacterAvatarScreen.component.ImagePrivacySettingsBottomSheet
import com.ensodai.avalon.presentation.editCharacterAvatarScreen.component.TopBarEditCharacterAvatarScreen
import com.ensodai.avalon.presentation.editCharacterAvatarScreen.viewState.EditCharacterAvatarScreenViewState
import com.ensodai.avalon.presentation.navigation.Navigation
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditCharacterAvatarScreenContent(
    navigation: Navigation,
    viewState: EditCharacterAvatarScreenViewState,
    actions: EditCharacterAvatarScreenActions,
    onEnableSwipeable: () -> Unit,
    onDisableSwipeable: () -> Unit
) {
    val colors = AvalonTheme.colors
    AlertDialogPermissionImage(
        isShowPermissionDialog = viewState.isShowPermissionDialog,
        onCloseButtonClick = { actions.onShowPermissionDialog(false) }
    )

    if (viewState.selectedImageToConfig != null) {
        ImagePrivacySettingsBottomSheet(
            image = viewState.selectedImageToConfig,
            onDismiss = { actions.onSelectImageToConfig(null) },
            onSave = { isPrivate, energyCost, requiredLevel ->
                actions.onUpdateImagePrivacy(viewState.selectedImageToConfig, isPrivate, energyCost, requiredLevel)
            }
        )
    }

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
        modifier = Modifier
            .fillMaxSize()
            .avalonCustomSwipe { dragDeltaX ->
                dragDeltaX > 0
            },
        topBar = {
            TopBarEditCharacterAvatarScreen(
                modifier = Modifier
                    .fillMaxWidth(),
                onBackButtonClick = { navigation.navigateBack() }
            )
        }
    ) { innerPaddings ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPaddings)
        ) {

            AddCharacterAvatarStageScreen(
                modifier = Modifier.weight(1f),
                characterAvatar = viewState.characterAvatar,
                characterImages = viewState.characterImages,
                pendingUploads = viewState.pendingImageUploads,
                isEnableAttachImages = viewState.loadingState != LoadingState.Loading,
                onAddImageClick = {},
                onShowPermissionDialog = { actions.onShowPermissionDialog(true) },
                onAttachImages = {
                    if (viewState.loadingState != LoadingState.Loading) {
                        actions.onAttachImages(it)
                    }
                },
                onStartAttachingFiles = {},
                onSelectImageAvatar = {
                    if (viewState.loadingState != LoadingState.Loading) {
                        actions.onSelectImageAvatar(it)
                    }
                },
                onDeleteImage = {
                    if (viewState.loadingState != LoadingState.Loading) {
                        actions.onDeleteImage(it)
                    }
                },
                onDeletePendingImage = { actions.onDeletePendingImage(it) },
                onImageSettingsClick = { actions.onSelectImageToConfig(it) }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            ) {
                if (viewState.pendingImageUploads.isNotEmpty()) {
                    Text(
                        text = "Загружаем изображения...",
                        color = colors.textSecondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TechnoButton(
                        text = stringResource(Res.string.cancel),
                        onClick = { navigation.navigateBack() },
                        isPrimary = false,
                        modifier = Modifier.weight(1f),
                        enabled = viewState.loadingState != LoadingState.Loading
                    )

                    TechnoButton(
                        text = stringResource(Res.string.save),
                        onClick = { actions.onUpdateImages() },
                        isPrimary = true,
                        isLoading = viewState.loadingState == LoadingState.Loading,
                        modifier = Modifier.weight(1f),
                        enabled = viewState.loadingState != LoadingState.Loading &&
                                viewState.pendingImageUploads.isEmpty()
                    )
                }
            }
        }
    }
}
