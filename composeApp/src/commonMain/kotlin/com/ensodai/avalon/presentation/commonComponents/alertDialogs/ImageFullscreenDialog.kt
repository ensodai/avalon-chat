package com.ensodai.avalon.presentation.commonComponents.alertDialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.ensodai.shared.avalon.chat.models.ImagePayload
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.image_saved_to_downloads
import com.ensodai.avalon.presentation.commonComponents.snackbar.LocalAppSnackbarController
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.utils.rememberImageDownloader
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.theme.AvalonTheme

/**
 * Диалог полноэкранного просмотра изображений с возможностью свайпа между несколькими изображениями
 *
 * @param imagePayloads Список URL изображений для просмотра
 * @param initialPageIndex Индекс начального изображения (по умолчанию 0)
 * @param onDismissRequest Колбек закрытия диалога
 */
@Composable
fun ImageFullscreenDialog(
    imagePayloads: List<ImagePayload>,
    initialPageIndex: Int = 0,
    onDismissRequest: () -> Unit
) {
    val colors = AvalonTheme.colors

    val coroutineScope = rememberCoroutineScope()
    val imageDownloader = rememberImageDownloader()
    val snackbarController = LocalAppSnackbarController.current

    val imagesCount = imagePayloads.size
    val pagerState = rememberPagerState(
        initialPage = initialPageIndex.coerceIn(0, imagesCount - 1),
        pageCount = { imagesCount }
    )
    val toastText = stringResource(Res.string.image_saved_to_downloads)
    val zoomState = rememberZoomState()

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
            // Горизонтальный свайп между изображениями
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->


                AsyncImage(
                    model = imagePayloads[page].url,
                    contentDescription = "Full screen image ${page + 1} of $imagesCount",
                    contentScale = ContentScale.Fit,
                    onSuccess = { state ->
                        zoomState.setContentSize(state.painter.intrinsicSize)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .zoomable(zoomState)
                )
            }

            // Кнопка закрытия
            IconButton(
                onClick = onDismissRequest,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 32.dp, end = 16.dp)
            ) {
                Icon(
                    imageVector = IconsManager.Close,
                    contentDescription = "Close",
                    tint = colors.textMain
                )
            }

            // Кнопка скачивания
            IconButton(
                onClick = {
                    val currentImageUrl = imagePayloads[pagerState.currentPage]
                    imageDownloader.downloadImage(currentImageUrl.url)
                    coroutineScope.launch {
                        snackbarController.showMessage(toastText)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 32.dp, end = 16.dp)
            ) {
                Icon(
                    imageVector = IconsManager.Download,
                    contentDescription = "Download",
                    tint = colors.textMain,
                    modifier = Modifier.size(28.dp)
                )
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
