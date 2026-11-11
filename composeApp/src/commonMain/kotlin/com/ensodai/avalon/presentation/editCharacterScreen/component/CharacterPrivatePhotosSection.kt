package com.ensodai.avalon.presentation.editCharacterScreen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.presentation.characterProfileScreen.viewState.CharacterAvatarWithSeed
import com.ensodai.avalon.presentation.commonComponents.GemCard
import com.ensodai.avalon.presentation.commonComponents.ShimmerAsyncImage
import com.ensodai.avalon.presentation.commonComponents.rememberAvatarStyle
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.edit_character_private_photos_title
import com.ensodai.avalon.generated.resources.add_paid_photos
import com.ensodai.avalon.generated.resources.add

@Composable
fun CharacterPrivatePhotosSection(
    modifier: Modifier = Modifier,
    images: List<CharacterAvatarWithSeed>,
    onEditPrivatePhotos: () -> Unit
) {
    val colors = AvalonTheme.colors
    GemCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        onClick = onEditPrivatePhotos,
        enabled = true,
        contentPadding = PaddingValues(16.dp)
    ) {
        if (images.isEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = IconsManager.Lock,
                    contentDescription = null,
                    tint = colors.textMain,
                    modifier = Modifier
                        .size(26.dp)
                        .alpha(0.9f)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.edit_character_private_photos_title).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.5.sp
                        ),
                        color = colors.textSecondary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = stringResource(Res.string.add_paid_photos),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = colors.textMain
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = IconsManager.Add,
                    contentDescription = stringResource(Res.string.add),
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
                        imageVector = IconsManager.Lock,
                        contentDescription = null,
                        tint = colors.textMain,
                        modifier = Modifier
                            .size(26.dp)
                            .alpha(0.9f)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = stringResource(Res.string.edit_character_private_photos_title).uppercase(),
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

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    images.take(4).forEach { avatarWithSeed ->
                        val avatarStyle = rememberAvatarStyle(avatarWithSeed.seed)

                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(avatarStyle.backgroundBrush),
                            contentAlignment = Alignment.Center
                        ) {
                            ShimmerAsyncImage(
                                modifier = Modifier.matchParentSize(),
                                model = avatarWithSeed.imageUrl,
                                seed = avatarWithSeed.seed,
                                blurhash = avatarWithSeed.thumbHash
                            )
                        }
                    }
                }
            }
        }
    }
}
