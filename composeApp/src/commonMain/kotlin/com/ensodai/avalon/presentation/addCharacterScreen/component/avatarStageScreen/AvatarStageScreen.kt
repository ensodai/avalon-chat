package com.ensodai.avalon.presentation.addCharacterScreen.component.avatarStageScreen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.add_profile_image_description
import com.ensodai.avalon.generated.resources.profile_image
import com.ensodai.avalon.presentation.addCharacterScreen.viewState.PendingImageUpload
import com.ensodai.avalon.presentation.commonComponents.ImagePickerLauncher
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun AvatarStageScreen(
    modifier: Modifier = Modifier,
    characterAvatar: CharacterImageDTO?,
    characterImages: List<CharacterImageDTO>,
    onAddImageClick: () -> Unit,
    onShowPermissionDialog: () -> Unit,
    onAttachImages: (List<CharacterImageDTO>) -> Unit,
    onStartAttachingFiles: () -> Unit,
    onSelectImageAvatar: (CharacterImageDTO) -> Unit,
    onDeleteImage: (CharacterImageDTO) -> Unit,
    isScrollableContent: Boolean = false,
    isEnableAttachImages: Boolean = true
) {
    val colors = AvalonTheme.colors
    ImagePickerLauncher(
        attachedImagesSize = characterImages.size,
        onAttachImages = { onAttachImages(it) },
        onShowPermissionDialog = { onShowPermissionDialog() },
        onStartAttachingFiles = { onStartAttachingFiles() }
    ) { launchImagePicker ->

        Column(
            modifier = modifier
                .fillMaxWidth()
        ) {

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = stringResource(Res.string.profile_image),
                    style = TextStyle(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textMain
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.add_profile_image_description),
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .then(
                        if (isScrollableContent) {
                            Modifier.aspectRatio(1f)
                        } else {
                            Modifier.weight(1f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (characterAvatar != null) {
                    HeroAvatarDisplay(
                        imageUrl = characterAvatar.url,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (characterImages.isEmpty()) {
                    BigAddPlaceholder(
                        onClick = {
                            if (!isEnableAttachImages) return@BigAddPlaceholder
                            launchImagePicker()
                            onAddImageClick()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (characterImages.isNotEmpty() || characterAvatar != null) {
                ThumbnailSelectorRow(
                    characterImages = characterImages,
                    currentAvatar = characterAvatar,
                    onAddImage = {
                        if (!isEnableAttachImages) return@ThumbnailSelectorRow
                        launchImagePicker()
                    },
                    onSelectImage = {
                        if (!isEnableAttachImages) return@ThumbnailSelectorRow
                        onSelectImageAvatar(it)
                    },
                    onDeleteImage = {
                        if (!isEnableAttachImages) return@ThumbnailSelectorRow
                        onDeleteImage(it)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun HeroAvatarDisplay(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AvalonTheme.colors.background)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Selected Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun BigAddPlaceholder(onClick: () -> Unit) {
    val colors = AvalonTheme.colors
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth(0.8f)
            .clickable { onClick() }
            .drawBehind {
                val strokeWidth = 2.dp.toPx()
                val dashWidth = 12.dp.toPx()
                val dashGap = 8.dp.toPx()
                val cornerRadius = 24.dp.toPx()

                drawRoundRect(
                    color = colors.textSecondary,
                    style = Stroke(
                        width = strokeWidth,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashWidth, dashGap), 0f)
                    ),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = IconsManager.Add,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Upload Image", color = colors.textSecondary, fontSize = 18.sp)
        }
    }
}

@Composable
fun ThumbnailSelectorRow(
    characterImages: List<CharacterImageDTO>,
    pendingUploads: List<PendingImageUpload> = emptyList(),
    currentAvatar: CharacterImageDTO?,
    unSelectableImageMode: Boolean = false,
    onAddImage: () -> Unit,
    onSelectImage: (CharacterImageDTO) -> Unit = {},
    onDeleteImage: (CharacterImageDTO) -> Unit,
    onDeletePendingImage: (String) -> Unit = {},
    onImageSettingsClick: (CharacterImageDTO) -> Unit = {}
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {

        item {
            AddThumbnailButton(onClick = onAddImage)
        }

        items(characterImages, key = { it.imagePublicId }) { image ->
            ThumbnailItem(
                image = image,
                isSelected = image == currentAvatar,
                unSelectableImageMode = unSelectableImageMode,
                onSelect = { onSelectImage(image) },
                onDelete = { onDeleteImage(image) },
                onSettingsClick = { onImageSettingsClick(image) }
            )
        }

        items(pendingUploads, key = { it.id }) { pending ->
            PendingThumbnailItem(
                fileName = pending.fileName,
                onDelete = { onDeletePendingImage(pending.id) }
            )
        }
    }
}

@Composable
fun AddThumbnailButton(onClick: () -> Unit) {
    val colors = AvalonTheme.colors
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = colors.strokeActive,
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = IconsManager.Add,
            contentDescription = "Add",
            tint = colors.strokeActive,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun ThumbnailItem(
    image: CharacterImageDTO,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    onSettingsClick: () -> Unit,
    unSelectableImageMode: Boolean
) {
    val colors = AvalonTheme.colors
    val alpha by animateFloatAsState(
        targetValue = if (isSelected || unSelectableImageMode) 1f else 0.5f,
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .size(80.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onSelect() }
                .then(
                    if (isSelected) {
                        Modifier.border(
                            2.dp,
                            colors.strokeActive,
                            RoundedCornerShape(16.dp)
                        )
                    } else {
                        Modifier
                    }
                )
        ) {
            AsyncImage(
                model = image.url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { this.alpha = alpha }
            )
        }

        // Кнопка настроек в левом верхнем углу
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(colors.overlaySurface.copy(alpha = 0.8f))
                .clickable { onSettingsClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = IconsManager.Settings,
                contentDescription = "Settings",
                tint = colors.textMain,
                modifier = Modifier.size(12.dp)
            )
        }

        // Индикатор блокировки в левом нижнем углу
        if (image.isPrivate || image.requiredLevel != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(colors.neonAccent.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (image.requiredLevel != null) IconsManager.Lock else IconsManager.Bolt,
                    contentDescription = "Premium/Locked",
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                )
            }
        }

        // Кнопка удаления в правом верхнем углу
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(colors.overlaySurface.copy(alpha = 0.8f))
                .clickable { onDelete() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = IconsManager.Close,
                contentDescription = "Delete",
                tint = colors.textMain,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun PendingThumbnailItem(
    fileName: String,
    onDelete: () -> Unit
) {
    val colors = AvalonTheme.colors
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceHigh),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = fileName,
                color = colors.textSecondary,
                fontSize = 10.sp,
                maxLines = 2
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(colors.overlaySurface.copy(alpha = 0.8f))
                .clickable { onDelete() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = IconsManager.Close,
                contentDescription = "Cancel upload",
                tint = colors.textMain,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
