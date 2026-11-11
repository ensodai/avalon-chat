package com.ensodai.avalon.presentation.characterProfileScreen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.presentation.characterProfileScreen.viewState.CharacterAvatarWithSeed
import com.ensodai.avalon.presentation.commonComponents.GemCard
import com.ensodai.avalon.presentation.commonComponents.ShimmerAsyncImage
import com.ensodai.avalon.presentation.commonComponents.alertDialogs.PremiumImageFullscreenDialog
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.premium_gallery_title
import com.ensodai.avalon.generated.resources.locked_desc
import com.ensodai.avalon.generated.resources.level_short
import kotlin.uuid.Uuid

@Composable
fun PremiumGallerySection(
    premiumPhotos: List<CharacterAvatarWithSeed>,
    onUnlockClick: suspend (Uuid) -> Result<Unit>,
    onNotEnoughEnergy: () -> Unit
) {
    if (premiumPhotos.isEmpty()) return

    val colors = AvalonTheme.colors
    var activeFullScreenPhotoIndex by remember { mutableStateOf<Int?>(null) }

    GemCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        enabled = false,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = IconsManager.Lock,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(Res.string.premium_gallery_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val chunkedPhotos = remember(premiumPhotos) { premiumPhotos.chunked(3) }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                chunkedPhotos.forEach { rowPhotos ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowPhotos.forEach { photo ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        activeFullScreenPhotoIndex = premiumPhotos.indexOf(photo)
                                    }
                            ) {
                                // Загружаем либо реальную картинку (если разблокировано), либо блюр (если заблокировано)
                                ShimmerAsyncImage(
                                    model = if (photo.isUnlocked) photo.imageUrl else "",
                                    seed = photo.seed,
                                    blurhash = photo.thumbHash,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Если картинка закрыта — рисуем замок и оверлей
                                if (!photo.isUnlocked) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.35f))
                                    )

                                    // Показываем замок посередине
                                    Icon(
                                        imageVector = IconsManager.Lock,
                                        contentDescription = stringResource(Res.string.locked_desc),
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier
                                            .size(24.dp)
                                            .align(Alignment.Center)
                                    )

                                    // Требование внизу фотографии
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 6.dp)
                                            .background(
                                                color = Color.Black.copy(alpha = 0.6f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            if (photo.requiredLevel != null) {
                                                Icon(
                                                    imageVector = IconsManager.Lock,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(10.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = stringResource(Res.string.level_short, photo.requiredLevel),
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = IconsManager.Bolt,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(10.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = "${photo.energyCost}",
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Дополняем последнюю строку пустыми Spacer-ами, чтобы элементы не растягивались
                        repeat(3 - rowPhotos.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    activeFullScreenPhotoIndex?.let { index ->
        PremiumImageFullscreenDialog(
            premiumPhotos = premiumPhotos,
            initialPageIndex = index,
            onUnlockClick = onUnlockClick,
            onDismissRequest = { activeFullScreenPhotoIndex = null },
            onNotEnoughEnergy = onNotEnoughEnergy
        )
    }
}
