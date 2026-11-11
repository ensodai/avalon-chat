package com.ensodai.avalon.presentation.chatVariantScreen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.cancel
import com.ensodai.avalon.generated.resources.change_image
import com.ensodai.avalon.generated.resources.change_story_name
import com.ensodai.avalon.generated.resources.clear
import com.ensodai.avalon.generated.resources.clear_chat
import com.ensodai.avalon.generated.resources.delete
import com.ensodai.avalon.generated.resources.delete_story
import com.ensodai.avalon.generated.resources.save
import com.ensodai.avalon.generated.resources.story
import com.ensodai.avalon.presentation.chatVariantScreen.action.BottomSheetActions
import com.ensodai.avalon.presentation.chatVariantScreen.viewState.BottomSheet
import com.ensodai.avalon.presentation.chatVariantScreen.viewState.BottomSheetState
import com.ensodai.avalon.presentation.commonComponents.ImagePickerLauncher
import com.ensodai.avalon.presentation.core.LoadingState
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetMenu(
    state: BottomSheetState,
    actions: BottomSheetActions,
    loadingState: LoadingState
) {
    when (state.bottomSheet) {
        BottomSheet.Closed -> {}
        BottomSheet.MainMenuBottomSheet -> {
            MainMenuBottomSheet(
                state = state,
                actions = actions
            )
        }

        BottomSheet.RenameBottomSheet -> {
            RenameBottomSheet(
                state = state,
                actions = actions,
                loadingState = loadingState
            )
        }

        BottomSheet.DeleteBottomSheet -> {
            DeleteBottomSheet(
                state = state,
                actions = actions,
                loadingState = loadingState
            )
        }

        BottomSheet.UpdateImageBottomSheet -> {
            UpdateImageBottomSheet(
                state = state,
                actions = actions,
                loadingState = loadingState
            )
        }

        BottomSheet.ClearChatStoryBottomSheet -> {
            ClearChatStoryBottomSheet(
                state = state,
                actions = actions,
                loadingState = loadingState
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainMenuBottomSheet(
    state: BottomSheetState,
    actions: BottomSheetActions
) {
    val colors = AvalonTheme.colors
    ModalBottomSheet(
        onDismissRequest = {
            actions.onCloseBottomSheet()
        },
        containerColor = colors.background,
        contentColor = colors.textMain,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            val title = state.selectedStory?.name ?: stringResource(Res.string.story)
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = colors.textMain,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            ListItem(
                modifier = Modifier.clickable {
                    actions.onShowRenameBottomSheet()
                },
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent,
                    headlineColor = colors.textMain,
                    leadingIconColor = colors.textMain
                ),
                headlineContent = { Text(stringResource(Res.string.change_story_name)) },
                leadingContent = {
                    Icon(
                        imageVector = IconsManager.Edit,
                        contentDescription = stringResource(Res.string.change_story_name)
                    )
                }
            )

            ListItem(
                modifier = Modifier.clickable {
                    actions.onShowUpdateImageBottomSheet()
                },
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent,
                    headlineColor = colors.textMain,
                    leadingIconColor = colors.textMain
                ),
                headlineContent = { Text(stringResource(Res.string.change_image)) },
                leadingContent = {
                    Icon(
                        imageVector = IconsManager.Image,
                        contentDescription = stringResource(Res.string.change_image)
                    )
                }
            )

            ListItem(
                modifier = Modifier.clickable {
                    actions.onShowClearStoryChatBottomSheet()
                },
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent,
                    headlineColor = colors.textMain,
                    leadingIconColor = colors.textMain
                ),
                headlineContent = { Text(stringResource(Res.string.clear_chat)) },
                leadingContent = {
                    Icon(
                        imageVector = IconsManager.DeleteOutline,
                        contentDescription = stringResource(Res.string.clear_chat)
                    )
                }
            )

            if (state.isEnableDeleteButton) {
                ListItem(
                    modifier = Modifier.clickable {
                        actions.onShowDeleteStoryBottomSheet()
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent,
                        headlineColor = colors.error,
                        leadingIconColor = colors.error
                    ),
                    headlineContent = {
                        Text(
                            text = stringResource(Res.string.delete),
                            color = colors.error
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = IconsManager.Delete,
                            contentDescription = stringResource(Res.string.delete),
                            tint = colors.error
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RenameBottomSheet(
    state: BottomSheetState,
    actions: BottomSheetActions,
    loadingState: LoadingState
) {
    val colors = AvalonTheme.colors
    var text by remember(state.selectedStory?.name) { mutableStateOf(state.selectedStory?.name.orEmpty()) }
    ModalBottomSheet(
        onDismissRequest = {
            actions.onCloseBottomSheet()
        },
        containerColor = colors.background,
        contentColor = colors.textMain,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = stringResource(Res.string.change_story_name),
                style = MaterialTheme.typography.titleLarge,
                color = colors.textMain,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                value = text,
                isError = text.trim().length < 3,
                onValueChange = { value ->
                    if (value.length <= 50) {
                        text = value
                    }
                },
                label = { Text(stringResource(Res.string.change_story_name)) },
                maxLines = 1,
                singleLine = true,
                enabled = loadingState != LoadingState.Loading
            )

            Spacer(modifier = Modifier.height(8.dp))

            ListItem(
                modifier = Modifier.clickable {
                    if (loadingState != LoadingState.Loading && text.trim().length >= 3) {
                        actions.onRenameStory(text.trim())
                    }
                },
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent,
                    headlineColor = if (loadingState == LoadingState.Loading || text.trim().length < 3) colors.textMain.copy(alpha = 0.4f) else colors.textMain
                ),
                headlineContent = {
                    Text(
                        text = stringResource(Res.string.save),
                        color = if (loadingState == LoadingState.Loading || text.trim().length < 3) colors.textMain.copy(alpha = 0.4f) else colors.textMain
                    )
                },
                leadingContent = {
                    if (loadingState == LoadingState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = colors.textMain,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = IconsManager.Save,
                            contentDescription = stringResource(Res.string.save),
                            tint = if (text.trim().length < 3) colors.textMain.copy(alpha = 0.4f) else colors.textMain
                        )
                    }
                }
            )
            ListItem(
                modifier = Modifier.clickable {
                    if (loadingState != LoadingState.Loading) {
                        actions.onCloseBottomSheet()
                    }
                },
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent,
                    headlineColor = colors.textMain
                ),
                headlineContent = {
                    Text(
                        text = stringResource(Res.string.cancel),
                        color = if (loadingState == LoadingState.Loading) colors.textMain.copy(alpha = 0.4f) else colors.textMain
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = IconsManager.Cancel,
                        contentDescription = stringResource(Res.string.cancel),
                        tint = if (loadingState == LoadingState.Loading) colors.textMain.copy(alpha = 0.4f) else colors.textMain
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteBottomSheet(
    state: BottomSheetState,
    actions: BottomSheetActions,
    loadingState: LoadingState
) {
    val colors = AvalonTheme.colors
    ModalBottomSheet(
        onDismissRequest = {
            actions.onCloseBottomSheet()
        },
        containerColor = colors.background,
        contentColor = colors.textMain,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            val text = stringResource(Res.string.delete_story)
            Text(
                text = "$text ${state.selectedStory?.name.orEmpty()}",
                style = MaterialTheme.typography.titleLarge,
                color = colors.textMain,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            ListItem(
                modifier = Modifier.clickable {
                    if (loadingState != LoadingState.Loading) {
                        actions.onDeleteStory()
                    }
                },
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent,
                    headlineColor = colors.error,
                    leadingIconColor = colors.error
                ),
                headlineContent = {
                    Text(
                        text = stringResource(Res.string.delete),
                        color = colors.error
                    )
                },
                leadingContent = {
                    if (loadingState == LoadingState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = colors.error,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = IconsManager.Delete,
                            contentDescription = stringResource(Res.string.delete),
                            tint = colors.error
                        )
                    }
                }
            )
            ListItem(
                modifier = Modifier.clickable {
                    if (loadingState != LoadingState.Loading) {
                        actions.onCloseBottomSheet()
                    }
                },
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent,
                    headlineColor = colors.textMain
                ),
                headlineContent = {
                    Text(
                        text = stringResource(Res.string.cancel),
                        color = colors.textMain
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = IconsManager.Cancel,
                        contentDescription = stringResource(Res.string.cancel),
                        tint = colors.textMain
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UpdateImageBottomSheet(
    state: BottomSheetState,
    actions: BottomSheetActions,
    loadingState: LoadingState
) {
    val colors = AvalonTheme.colors
    ModalBottomSheet(
        onDismissRequest = {
            actions.onCloseBottomSheet()
        },
        containerColor = colors.background,
        contentColor = colors.textMain,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = stringResource(Res.string.change_image),
                style = MaterialTheme.typography.titleLarge,
                color = colors.textMain,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (state.attachedImage != null) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        AsyncImage(
                            model = state.attachedImage.url,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    if (loadingState != LoadingState.Loading) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 8.dp, y = (-8).dp)
                                .size(24.dp)
                                .shadow(elevation = 2.dp, shape = CircleShape)
                                .background(colors.textMain, CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = false),
                                    onClick = { actions.onDetachImage() }
                                )
                        ) {
                            Icon(
                                imageVector = IconsManager.Close,
                                contentDescription = null,
                                tint = Color.Black.copy(alpha = 0.8f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            } else {
                ImagePickerLauncher(
                    attachedImagesSize = 0,
                    onAttachImages = { actions.onAttachImage(it) },
                    onShowPermissionDialog = { actions.onShowPermissionDialog() },
                    onStartAttachingFiles = { },
                    maxItems = 1
                ) { launchImagePicker ->
                    DashedRoundedRectBox(
                        modifier = Modifier
                            .size(160.dp)
                            .padding(horizontal = 16.dp),
                        onAddImageClick = { launchImagePicker() }
                    )
                }
            }
            ListItem(
                modifier = Modifier.clickable {
                    if (loadingState != LoadingState.Loading) {
                        actions.onUpdateImage()
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                headlineContent = {
                    Text(
                        text = stringResource(Res.string.save),
                        color = if (loadingState == LoadingState.Loading) colors.textMain.copy(alpha = 0.6f) else colors.textMain
                    )
                },
                leadingContent = {
                    if (loadingState == LoadingState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = colors.textMain,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = IconsManager.Save,
                            contentDescription = stringResource(Res.string.save),
                            tint = colors.textMain
                        )
                    }
                }
            )
            ListItem(
                modifier = Modifier.clickable {
                    if (loadingState != LoadingState.Loading) {
                        actions.onCloseBottomSheet()
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                headlineContent = {
                    Text(
                        text = stringResource(Res.string.cancel),
                        color = if (loadingState == LoadingState.Loading) colors.textMain.copy(alpha = 0.6f) else colors.textMain
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = IconsManager.Cancel,
                        contentDescription = stringResource(Res.string.cancel),
                        tint = if (loadingState == LoadingState.Loading) colors.textMain.copy(alpha = 0.6f) else colors.textMain
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClearChatStoryBottomSheet(
    state: BottomSheetState,
    actions: BottomSheetActions,
    loadingState: LoadingState
) {
    val colors = AvalonTheme.colors
    ModalBottomSheet(
        onDismissRequest = {
            actions.onCloseBottomSheet()
        },
        containerColor = colors.background,
        contentColor = colors.textMain,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = stringResource(Res.string.clear_chat),
                style = MaterialTheme.typography.titleLarge,
                color = colors.textMain,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            ListItem(
                modifier = Modifier.clickable {
                    if (loadingState != LoadingState.Loading) {
                        actions.onClearStoryChat()
                    }
                },
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent,
                    headlineColor = colors.error,
                    leadingIconColor = colors.error
                ),
                headlineContent = {
                    Text(
                        text = stringResource(Res.string.clear),
                        color = colors.error
                    )
                },
                leadingContent = {
                    if (loadingState == LoadingState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = colors.error,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = IconsManager.Delete,
                            contentDescription = stringResource(Res.string.clear),
                            tint = colors.error
                        )
                    }
                }
            )
            ListItem(
                modifier = Modifier.clickable {
                    if (loadingState != LoadingState.Loading) {
                        actions.onCloseBottomSheet()
                    }
                },
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent,
                    headlineColor = colors.textMain
                ),
                headlineContent = {
                    Text(
                        text = stringResource(Res.string.cancel),
                        color = colors.textMain
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = IconsManager.Cancel,
                        contentDescription = stringResource(Res.string.cancel),
                        tint = colors.textMain
                    )
                }
            )
        }
    }
}

