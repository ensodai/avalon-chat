package com.ensodai.avalon.presentation.addCharacterScreen.component.helloCardStageScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.data.db.dto.WelcomeCardImageDto
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.description
import com.ensodai.avalon.generated.resources.welcome_card_stage
import com.ensodai.avalon.generated.resources.welcome_card_stage_description
import com.ensodai.avalon.presentation.addCharacterScreen.component.avatarStageScreen.ThumbnailSelectorRow
import com.ensodai.avalon.presentation.addCharacterScreen.viewState.PendingImageUpload
import com.ensodai.avalon.presentation.commonComponents.textFields.TechnoInput
import com.ensodai.avalon.presentation.commonComponents.welcomCard.CardColor
import com.ensodai.avalon.presentation.commonComponents.welcomCard.WelcomeCard
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.utils.EMPTY_STRING
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun AddCharacterHelloCardStageScreen(
    text: String?,
    images: List<WelcomeCardImageDto>?,
    pendingUploads: List<PendingImageUpload>,
    startColor: CardColor,
    endColor: CardColor,
    isShowPreviewCard: Boolean,
    allowedColors: List<CardColor>,
    isEnableLight: Boolean,
    onChangeDescription: (String) -> Unit,
    onAttachImages: (List<PlatformFile>) -> Unit,
    onShowPermissionDialog: () -> Unit,
    onDeleteImage: (WelcomeCardImageDto) -> Unit,
    onDeletePendingImage: (String) -> Unit = {},
    onSelectStartTone: (CardColor) -> Unit,
    onSelectEndTone: (CardColor) -> Unit,
    onToggleLight: () -> Unit
) {
    val colors = AvalonTheme.colors
    val remainingSlots = (3 - (images?.size ?: 0) - pendingUploads.size).coerceAtLeast(0)
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
        modifier = Modifier
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(Res.string.welcome_card_stage),
                style = TextStyle(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textMain
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.welcome_card_stage_description),
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            AddCharacterWelcomeCardImagesLazyRow(
                imageItems = images,
                pendingUploads = pendingUploads,
                onAddImage = {
                    if (remainingSlots <= 0) return@AddCharacterWelcomeCardImagesLazyRow
                    pickerLauncher.launch()
                },
                onDeleteImage = onDeleteImage,
                onDeletePendingImage = onDeletePendingImage
            )

            TechnoInput(
                value = text ?: EMPTY_STRING,
                onValueChange = { newValue ->
                    if (newValue.length <= 8000) {
                        onChangeDescription(newValue)
                    } else {
                        onChangeDescription(newValue.take(8000))
                    }
                },
                placeholder = stringResource(Res.string.description),
                maxLines = 8,
                minLines = 8,
                maxLength = 8000,
                isError = false
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (isShowPreviewCard) {
            AddCharacterPreviewCard(
                text = text,
                images = images,
                startColor = startColor,
                endColor = endColor,
                allowedColors = allowedColors,
                isEnableLight = isEnableLight,
                onSelectStartTone = onSelectStartTone,
                onSelectEndTone = onSelectEndTone,
                onToggleLight = onToggleLight
            )
        }
    }
}

@Composable
private fun AddCharacterPreviewCard(
    text: String?,
    images: List<WelcomeCardImageDto>?,
    startColor: CardColor,
    endColor: CardColor,
    allowedColors: List<CardColor>,
    isEnableLight: Boolean,
    onSelectStartTone: (CardColor) -> Unit,
    onSelectEndTone: (CardColor) -> Unit,
    onToggleLight: () -> Unit
) {
    Column {
        WelcomeCard(
            text = text,
            images = images,
            isEnableLight = isEnableLight,
            startColor = startColor,
            endColor = endColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        ColorSelector(
            colors = allowedColors,
            selectedColor = startColor,
            onColorSelected = onSelectStartTone,
        )
        Spacer(modifier = Modifier.height(4.dp))
        ColorSelector(
            colors = allowedColors,
            selectedColor = endColor,
            onColorSelected = onSelectEndTone
        )
        Spacer(modifier = Modifier.height(4.dp))
        AddCharacterBacklightToggle(
            isEnabled = isEnableLight,
            onToggle = onToggleLight
        )
    }
}

@Composable
private fun AddCharacterBacklightToggle(
    isEnabled: Boolean,
    onToggle: () -> Unit
) {
    var checked by remember { mutableStateOf(isEnabled) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Icon(
            modifier = Modifier.size(32.dp),
            imageVector = IconsManager.TipsAndUpdates,
            contentDescription = if (checked) "Light enabled" else "Light disabled",
        )
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = {
                checked = it
                onToggle()
            }
        )
    }
}

@Composable
private fun AddCharacterWelcomeCardImagesLazyRow(
    imageItems: List<WelcomeCardImageDto>?,
    pendingUploads: List<PendingImageUpload>,
    onAddImage: () -> Unit,
    onDeleteImage: (WelcomeCardImageDto) -> Unit,
    onDeletePendingImage: (String) -> Unit
) {
    val images = imageItems?.map {
        CharacterImageDTO(
            imagePublicId = it.imagePublicId,
            url = it.url,
            thumbHash = it.thumbHash
        )
    } ?: emptyList()

    ThumbnailSelectorRow(
        characterImages = images,
        pendingUploads = pendingUploads,
        currentAvatar = null,
        unSelectableImageMode = true,
        onAddImage = onAddImage,
        onSelectImage = {},
        onDeletePendingImage = onDeletePendingImage,
        onDeleteImage = {
            onDeleteImage(
                WelcomeCardImageDto(
                    imagePublicId = it.imagePublicId,
                    url = it.url,
                    thumbHash = it.thumbHash
                )
            )
        }
    )
}
