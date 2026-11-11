package com.ensodai.avalon.presentation.editCharacterPrivatePhotosScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.add_photo_desc
import com.ensodai.avalon.generated.resources.cancel
import com.ensodai.avalon.generated.resources.delete
import com.ensodai.avalon.generated.resources.edit
import com.ensodai.avalon.generated.resources.edit_character_private_photos_desc
import com.ensodai.avalon.generated.resources.edit_character_private_photos_title
import com.ensodai.avalon.generated.resources.energy_desc
import com.ensodai.avalon.generated.resources.private_photo_desc
import com.ensodai.avalon.generated.resources.save
import com.ensodai.avalon.generated.resources.uploading_new_images
import com.ensodai.avalon.navigation.AvalonBackHandler
import com.ensodai.avalon.navigation.avalonCustomSwipe
import com.ensodai.avalon.presentation.commonComponents.buttons.TechnoButton
import com.ensodai.avalon.presentation.core.LoadingState
import com.ensodai.avalon.presentation.editCharacterPrivatePhotosScreen.action.EditCharacterPrivatePhotosScreenActions
import com.ensodai.avalon.presentation.editCharacterPrivatePhotosScreen.component.PrivateImageSettingsBottomSheet
import com.ensodai.avalon.presentation.editCharacterPrivatePhotosScreen.viewState.EditCharacterPrivatePhotosScreenViewState
import com.ensodai.avalon.presentation.editCharacterAvatarScreen.component.TopBarEditCharacterAvatarScreen
import com.ensodai.avalon.presentation.navigation.Navigation
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.theme.AvalonTheme
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditCharacterPrivatePhotosScreenContent(
    navigation: Navigation,
    viewState: EditCharacterPrivatePhotosScreenViewState,
    actions: EditCharacterPrivatePhotosScreenActions,
    onEnableSwipeable: () -> Unit,
    onDisableSwipeable: () -> Unit
) {
    val colors = AvalonTheme.colors

    if (viewState.selectedImageToConfig != null) {
        PrivateImageSettingsBottomSheet(
            image = viewState.selectedImageToConfig,
            onDismiss = { actions.onSelectImageToConfig(null) },
            onSave = { cost ->
                actions.onUpdateImagePrice(viewState.selectedImageToConfig, cost)
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

    val remainingSlots = (100 - viewState.allImages.size - viewState.pendingImageUploads.size).coerceAtLeast(0)
    val pickerLauncher = rememberFilePickerLauncher(
        type = FileKitType.Image,
        mode = FileKitMode.Multiple(maxItems = remainingSlots.takeIf { it > 0 } ?: 1),
        onResult = { files ->
            val pickedFiles = files.orEmpty()
            if (pickedFiles.isNotEmpty()) {
                actions.onAttachImages(pickedFiles)
            }
        }
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .avalonCustomSwipe { dragDeltaX ->
                dragDeltaX > 0
            },
        topBar = {
            TopBarEditCharacterAvatarScreen(
                modifier = Modifier.fillMaxWidth(),
                onBackButtonClick = { navigation.navigateBack() }
            )
        }
    ) { innerPaddings ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPaddings)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = stringResource(Res.string.edit_character_private_photos_title),
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textMain
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.edit_character_private_photos_desc),
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Кнопка «Добавить» в начале сетки
                item {
                    AddImageTile(
                        isEnabled = viewState.loadingState != LoadingState.Loading && remainingSlots > 0,
                        onClick = { pickerLauncher.launch() }
                    )
                }

                // Картинки в процессе загрузки
                items(viewState.pendingImageUploads) { pending ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.cardBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = colors.neonAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Загруженные приватные картинки
                items(viewState.characterImages) { image ->
                    PrivateImageTile(
                        image = image,
                        onClick = {
                            if (viewState.loadingState != LoadingState.Loading) {
                                actions.onSelectImageToConfig(image)
                            }
                        },
                        onDelete = {
                            actions.onDeleteImage(image)
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            ) {
                if (viewState.pendingImageUploads.isNotEmpty()) {
                    Text(
                        text = stringResource(Res.string.uploading_new_images),
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
                        modifier = Modifier.weight(1f),
                        enabled = viewState.loadingState != LoadingState.Loading && viewState.pendingImageUploads.isEmpty()
                    )
                }
            }
        }
    }
}

@Composable
fun AddImageTile(
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val colors = AvalonTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.cardBackground)
            .clickable(enabled = isEnabled, onClick = onClick)
            .drawBehind {
                drawRoundRect(
                    color = colors.strokeActive,
                    style = Stroke(
                        width = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    ),
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = IconsManager.Add,
            contentDescription = stringResource(Res.string.add_photo_desc),
            tint = colors.textSecondary,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun PrivateImageTile(
    image: CharacterImageDTO,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = AvalonTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.cardBackground)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = image.url,
            contentDescription = stringResource(Res.string.private_photo_desc),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Кнопка удаления (крестик) в правом верхнем углу
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { onDelete() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = IconsManager.Close,
                contentDescription = stringResource(Res.string.delete),
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }

        // Карандашик в левом нижнем углу — подсказка что можно нажать и отредактировать
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(6.dp)
                .size(26.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = IconsManager.Edit,
                contentDescription = stringResource(Res.string.edit),
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }

        // Отображение цены
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(6.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.7f))
                .border(1.dp, colors.strokeActive.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = IconsManager.Bolt,
                    contentDescription = stringResource(Res.string.energy_desc),
                    tint = colors.neonAccent,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = image.energyCost.toString(),
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
    }
}
