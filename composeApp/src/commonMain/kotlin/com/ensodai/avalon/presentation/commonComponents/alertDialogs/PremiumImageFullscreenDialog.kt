package com.ensodai.avalon.presentation.commonComponents.alertDialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.ensodai.avalon.data.utils.ThumbHashDecoder
import com.ensodai.avalon.presentation.characterProfileScreen.viewState.CharacterAvatarWithSeed
import com.ensodai.avalon.presentation.commonComponents.snackbar.LocalAppSnackbarController
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.utils.rememberImageDownloader
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.image_saved_to_downloads
import com.ensodai.avalon.generated.resources.premium_image_content_description
import com.ensodai.avalon.generated.resources.blurred_image_loading_desc
import com.ensodai.avalon.generated.resources.blurred_image_desc
import com.ensodai.avalon.generated.resources.available_from_level
import com.ensodai.avalon.generated.resources.unlock_photo
import com.ensodai.avalon.generated.resources.photo_available_on_level_up
import com.ensodai.avalon.generated.resources.unlock_photo_confirm_pattern
import com.ensodai.avalon.generated.resources.required_level_pattern
import com.ensodai.avalon.generated.resources.unlock_for_energy_pattern
import com.ensodai.avalon.generated.resources.close
import com.ensodai.avalon.generated.resources.download
import com.ensodai.avalon.domain.useCases.characterProfileScreenUseCase.InsufficientEnergyException
import kotlin.uuid.Uuid

/**
 * Диалог полноэкранного просмотра премиум-изображений персонажа.
 * Поддерживает горизонтальные свайпы, отображение заблюренных фото и покупку/инфо о требуемом уровне прямо из галереи.
 */
@Composable
fun PremiumImageFullscreenDialog(
    premiumPhotos: List<CharacterAvatarWithSeed>,
    initialPageIndex: Int = 0,
    onUnlockClick: suspend (Uuid) -> Result<Unit>,
    onDismissRequest: () -> Unit,
    onNotEnoughEnergy: () -> Unit
) {
    val colors = AvalonTheme.colors
    val coroutineScope = rememberCoroutineScope()
    val imageDownloader = rememberImageDownloader()
    val snackbarController = LocalAppSnackbarController.current
    val toastText = stringResource(Res.string.image_saved_to_downloads)

    val imagesCount = premiumPhotos.size
    val pagerState = rememberPagerState(
        initialPage = initialPageIndex.coerceIn(0, imagesCount - 1),
        pageCount = { imagesCount }
    )

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val photo = premiumPhotos[page]
                val zoomState = rememberZoomState()
                val isLocalLoading = remember(photo.id) { mutableStateOf(false) }
                var isImageLoaded by remember(photo.id) { mutableStateOf(false) }

                if (photo.isUnlocked) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Разблокированная фотография с возможностью зума
                        AsyncImage(
                            model = photo.imageUrl,
                            contentDescription = stringResource(Res.string.premium_image_content_description, page + 1, imagesCount),
                            contentScale = ContentScale.Fit,
                            onSuccess = { state ->
                                zoomState.setContentSize(state.painter.intrinsicSize)
                                isImageLoaded = true
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .zoomable(zoomState)
                        )

                        if (!isImageLoaded) {
                            // Закрытая фотография / блюр в процессе загрузки по сети
                            val thumbBitmap = remember(photo.thumbHash) {
                                photo.thumbHash?.let { ThumbHashDecoder.decodeString(it) }
                            }

                            if (thumbBitmap != null) {
                                Image(
                                    bitmap = thumbBitmap,
                                    contentDescription = stringResource(Res.string.blurred_image_loading_desc),
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.DarkGray)
                                )
                            }

                            // Затемняющий оверлей с крутящимся лоадером загрузки
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.45f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = colors.neonAccent,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Закрытая фотография: показываем блюр на основе thumbHash
                    val thumbBitmap = remember(photo.thumbHash) {
                        photo.thumbHash?.let { ThumbHashDecoder.decodeString(it) }
                    }

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (thumbBitmap != null) {
                            Image(
                                bitmap = thumbBitmap,
                                contentDescription = stringResource(Res.string.blurred_image_desc),
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.DarkGray)
                            )
                        }

                        // Затемняющий оверлей
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.45f))
                        )

                        // Карточка с замком и кнопкой покупки
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 32.dp)
                                .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = IconsManager.Lock,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(44.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = if (photo.requiredLevel != null) {
                                    stringResource(Res.string.available_from_level, photo.requiredLevel)
                                } else {
                                    stringResource(Res.string.unlock_photo)
                                },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (photo.requiredLevel != null) {
                                    stringResource(Res.string.photo_available_on_level_up)
                                } else {
                                    stringResource(Res.string.unlock_photo_confirm_pattern, photo.energyCost)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.LightGray,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            if (photo.requiredLevel != null) {
                                Box(
                                    modifier = Modifier
                                        .background(Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = stringResource(Res.string.required_level_pattern, photo.requiredLevel),
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            } else {
                                Button(
                                    onClick = {
                                        val photoId = photo.id
                                        if (photoId != null) {
                                            coroutineScope.launch {
                                                isLocalLoading.value = true
                                                onUnlockClick(photoId).onFailure { exception ->
                                                    exception.printStackTrace()
                                                    if (exception is InsufficientEnergyException) {
                                                        onNotEnoughEnergy()
                                                    }
                                                }
                                                isLocalLoading.value = false
                                            }
                                        }
                                    },
                                    enabled = !isLocalLoading.value,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.neonAccent,
                                        contentColor = Color.White,
                                        disabledContainerColor = colors.neonAccent.copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                                ) {
                                    if (isLocalLoading.value) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = IconsManager.Bolt,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = stringResource(Res.string.unlock_for_energy_pattern, photo.energyCost),
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Кнопка закрытия (сверху-справа)
            IconButton(
                onClick = onDismissRequest,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 32.dp, end = 16.dp)
            ) {
                Icon(
                    imageVector = IconsManager.Close,
                    contentDescription = stringResource(Res.string.close),
                    tint = colors.textMain
                )
            }

            // Кнопка скачивания (снизу-справа, показывается только если фото разблокировано)
            val currentPhoto = premiumPhotos[pagerState.currentPage]
            if (currentPhoto.isUnlocked) {
                IconButton(
                    onClick = {
                        currentPhoto.imageUrl?.let { url ->
                            imageDownloader.downloadImage(url)
                            coroutineScope.launch {
                                snackbarController.showMessage(toastText)
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 32.dp, end = 16.dp)
                ) {
                    Icon(
                        imageVector = IconsManager.Download,
                        contentDescription = stringResource(Res.string.download),
                        tint = colors.textMain,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Индикатор страниц (1/5)
            if (imagesCount > 1) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1} / $imagesCount",
                        color = colors.textMain,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
