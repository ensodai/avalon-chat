package com.ensodai.avalon.presentation.commonComponents

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import com.ensodai.avalon.data.utils.ThumbHashDecoder
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun ShimmerAsyncImage(
    modifier: Modifier = Modifier,
    model: Any?,
    seed: String,
    imageWidth: Int? = null,
    imageHeight: Int? = null,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    emojiSize: Dp = 28.dp,
    blurhash: String?
) {
    val avatarStyle = rememberAvatarStyle(seed)
    val thumbBitmap = remember(blurhash) {
        blurhash?.let { ThumbHashDecoder.decodeString(it) }
    }
    val aspectRatioModifier = remember(imageWidth, imageHeight) {
        if (imageWidth != null && imageHeight != null && imageHeight > 0) {
            Modifier.aspectRatio(imageWidth.toFloat() / imageHeight.toFloat())
        } else {
            Modifier
        }
    }

    Box(
        modifier = modifier
            .then(aspectRatioModifier)
            .clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        if (model == null || model.toString().isBlank()) {
            ImagePlaceholder(
                thumbBitmap = thumbBitmap,
                avatarStyle = avatarStyle,
                emojiSize = emojiSize,
                contentScale = contentScale
            )
        } else {
            val painter = rememberAsyncImagePainter(model = model)
            val state = painter.state.collectAsState().value

            when (state) {
                is AsyncImagePainter.State.Success -> {
                    Image(
                        painter = painter,
                        contentDescription = contentDescription,
                        contentScale = contentScale,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is AsyncImagePainter.State.Loading -> {
                    Box(modifier = Modifier.fillMaxSize().pulseLoading()) {
                        ImagePlaceholder(
                            thumbBitmap = thumbBitmap,
                            avatarStyle = avatarStyle,
                            emojiSize = emojiSize,
                            contentScale = contentScale
                        )
                    }
                }

                is AsyncImagePainter.State.Error,
                is AsyncImagePainter.State.Empty -> {
                    ImagePlaceholder(
                        thumbBitmap = thumbBitmap,
                        avatarStyle = avatarStyle,
                        emojiSize = emojiSize,
                        contentScale = contentScale
                    )
                }
            }
        }
    }
}

@Composable
private fun ImagePlaceholder(
    thumbBitmap: ImageBitmap?,
    avatarStyle: AvatarStyle,
    emojiSize: Dp,
    contentScale: ContentScale
) {
    val colors = AvalonTheme.colors
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (thumbBitmap != null) {
            Image(
                bitmap = thumbBitmap,
                contentDescription = null,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(avatarStyle.backgroundBrush)
            )
            Icon(
                imageVector = avatarStyle.icon,
                contentDescription = null,
                tint = colors.textMain.copy(alpha = 0.8f),
                modifier = Modifier.size(emojiSize)
            )
        }
    }
}

fun Modifier.pulseLoading(isLoading: Boolean = true): Modifier = composed {
    if (!isLoading) return@composed this

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    graphicsLayer {
        this.alpha = alpha
    }
}
