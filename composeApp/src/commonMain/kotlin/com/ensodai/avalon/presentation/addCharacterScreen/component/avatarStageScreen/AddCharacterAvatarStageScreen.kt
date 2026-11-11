package com.ensodai.avalon.presentation.addCharacterScreen.component.avatarStageScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.add_profile_image_description
import com.ensodai.avalon.generated.resources.profile_image
import com.ensodai.avalon.presentation.addCharacterScreen.viewState.PendingImageUpload
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun AddCharacterAvatarStageScreen(
    modifier: Modifier = Modifier,
    characterAvatar: CharacterImageDTO?,
    characterImages: List<CharacterImageDTO>,
    pendingUploads: List<PendingImageUpload>,
    onAddImageClick: () -> Unit,
    onShowPermissionDialog: () -> Unit,
    onAttachImages: (List<PlatformFile>) -> Unit,
    onStartAttachingFiles: () -> Unit,
    onSelectImageAvatar: (CharacterImageDTO) -> Unit,
    onDeleteImage: (CharacterImageDTO) -> Unit,
    onDeletePendingImage: (String) -> Unit = {},
    onImageSettingsClick: (CharacterImageDTO) -> Unit = {},
    isScrollableContent: Boolean = false,
    isEnableAttachImages: Boolean = true
) {
    val colors = AvalonTheme.colors
    val remainingSlots = (10 - characterImages.size - pendingUploads.size).coerceAtLeast(0)
    val pickerLauncher = rememberFilePickerLauncher(
        type = FileKitType.Image,
        mode = FileKitMode.Multiple(maxItems = remainingSlots.takeIf { it > 0 } ?: 1),
        onResult = { files ->
            val pickedFiles = files.orEmpty()
            if (pickedFiles.isNotEmpty()) {
                onAttachImages(pickedFiles)
            }
        }
    )

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
            } else if (pendingUploads.isNotEmpty()) {
                PendingHeroAvatarDisplay(
                    fileName = pendingUploads.first().fileName,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (characterImages.isEmpty()) {
                BigAddPlaceholder(
                    onClick = {
                        if (!isEnableAttachImages || remainingSlots <= 0) return@BigAddPlaceholder
                        onAddImageClick()
                        onStartAttachingFiles()
                        pickerLauncher.launch()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (characterImages.isNotEmpty() || characterAvatar != null || pendingUploads.isNotEmpty()) {
            ThumbnailSelectorRow(
                characterImages = characterImages,
                pendingUploads = pendingUploads,
                currentAvatar = characterAvatar,
                onAddImage = {
                    if (!isEnableAttachImages || remainingSlots <= 0) return@ThumbnailSelectorRow
                    onStartAttachingFiles()
                    pickerLauncher.launch()
                },
                onSelectImage = {
                    if (!isEnableAttachImages) return@ThumbnailSelectorRow
                    onSelectImageAvatar(it)
                },
                onDeleteImage = {
                    if (!isEnableAttachImages) return@ThumbnailSelectorRow
                    onDeleteImage(it)
                },
                onDeletePendingImage = { onDeletePendingImage(it) },
                onImageSettingsClick = onImageSettingsClick
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PendingHeroAvatarDisplay(
    fileName: String,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors
    Card(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = colors.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = colors.textMain)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Uploading...",
                color = colors.textMain,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = fileName,
                color = colors.textSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
