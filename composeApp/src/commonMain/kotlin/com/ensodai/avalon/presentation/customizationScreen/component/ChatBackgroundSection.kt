package com.ensodai.avalon.presentation.customizationScreen.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ensodai.avalon.domain.models.ChatBackgroundImage
import com.ensodai.avalon.generated.resources.*
import com.ensodai.avalon.presentation.commonComponents.GemCard
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChatBackgroundSection(
    modifier: Modifier = Modifier,
    backgroundImagePath: String?,
    backgroundBlur: Int,
    backgroundImages: List<ChatBackgroundImage>,
    onBackgroundImageSelected: (PlatformFile) -> Unit,
    onSelectBackgroundImage: (ChatBackgroundImage) -> Unit,
    onDeleteBackgroundImage: (ChatBackgroundImage) -> Unit,
    onBackgroundBlurChanged: (Int) -> Unit,
) {
    val colors = AvalonTheme.colors
    val remainingSlots = (10 - backgroundImages.size).coerceAtLeast(0)

    val launcher = rememberFilePickerLauncher(
        type = FileKitType.Image,
        mode = FileKitMode.Single,
        onResult = { file: PlatformFile? ->
            if (file != null) {
                onBackgroundImageSelected(file)
            }
        }
    )

    GemCard(
        modifier = modifier.fillMaxWidth(),
        onClick = { if (backgroundImages.isEmpty() && remainingSlots > 0) launcher.launch() },
        enabled = backgroundImages.isEmpty() && remainingSlots > 0,
        contentPadding = PaddingValues(16.dp)
    ) {
        if (backgroundImages.isEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = IconsManager.AddPhotoAlternate,
                    contentDescription = null,
                    tint = colors.textMain,
                    modifier = Modifier
                        .size(26.dp)
                        .alpha(0.9f)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.chat_background_label),
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.5.sp
                        ),
                        color = colors.textSecondary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = stringResource(Res.string.add_background),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = colors.textMain
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = IconsManager.Add,
                    contentDescription = "Add",
                    tint = colors.strokeSubtle,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = IconsManager.Image,
                        contentDescription = null,
                        tint = colors.textMain,
                        modifier = Modifier
                            .size(26.dp)
                            .alpha(0.9f)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = stringResource(Res.string.chat_background_label),
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.5.sp
                        ),
                        color = colors.textSecondary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = IconsManager.KeyboardArrowRight,
                        contentDescription = null,
                        tint = colors.strokeSubtle,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (remainingSlots > 0) {
                        item {
                            AddBackgroundButton(onClick = { launcher.launch() })
                        }
                    }

                    items(backgroundImages, key = { it.id }) { image ->
                        BackgroundThumbnailItem(
                            image = image,
                            isSelected = image.url == backgroundImagePath,
                            onSelect = { onSelectBackgroundImage(image) },
                            onDelete = { onDeleteBackgroundImage(image) }
                        )
                    }
                }

                if (backgroundImagePath != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(Res.string.background_blur),
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.textMain
                        )
                        Switch(
                            checked = backgroundBlur > 0,
                            onCheckedChange = { isChecked ->
                                onBackgroundBlurChanged(if (isChecked) 25 else 0)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.background,
                                checkedTrackColor = colors.neonAccent,
                                uncheckedThumbColor = colors.textSecondary,
                                uncheckedTrackColor = colors.cardBackground
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddBackgroundButton(onClick: () -> Unit) {
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
fun BackgroundThumbnailItem(
    image: ChatBackgroundImage,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = AvalonTheme.colors
    val alpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.5f,
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
