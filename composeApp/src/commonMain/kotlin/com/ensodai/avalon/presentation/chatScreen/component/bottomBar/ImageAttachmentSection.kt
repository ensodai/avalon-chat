package com.ensodai.avalon.presentation.chatScreen.component.bottomBar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.presentation.commonComponents.loadingIndicators.LoadingIndicator
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.theme.AvalonTheme

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ImageAttachmentSection(
    isLoadingImages: Boolean = false,
    attachedImages: List<CharacterImageDTO>,
    onDetachImages: (CharacterImageDTO) -> Unit
) {
    val colors = AvalonTheme.colors
    val currentState = when {
        isLoadingImages -> "loading"
        attachedImages.isNotEmpty() -> "files"
        else -> "empty"
    }

    AnimatedContent(
        targetState = currentState,
        transitionSpec = {
            (slideInVertically { height -> height } + fadeIn()).togetherWith(slideOutVertically { height -> -height } + fadeOut())
        }
    ) { state ->
        when (state) {
            "loading" -> {
                LoadingIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    isLoading = true,
                    size = 24
                )
            }

            "files" -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .animateContentSize()
                ) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(
                            items = attachedImages,
                            key = { it.toString() }
                        ) { filePath ->
                            val url = filePath.url
                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(100.dp)
                                    .animateItem()
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = url),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(colors.strokeSubtle)
                                )

                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(colors.textMain.copy(alpha = 0.6f))
                                        .clickable { onDetachImages(filePath) }
                                        .align(Alignment.TopEnd)
                                ) {
                                    Icon(
                                        imageVector = IconsManager.Close,
                                        contentDescription = "Detach file",
                                        modifier = Modifier
                                            .size(14.dp)
                                            .align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                Spacer(modifier = Modifier.size(0.dp))
            }
        }
    }
}
