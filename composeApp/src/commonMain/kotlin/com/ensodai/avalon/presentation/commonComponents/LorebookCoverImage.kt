package com.ensodai.avalon.presentation.commonComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager

@Composable
fun LorebookCoverImage(
    coverUrl: String?,
    lorebookId: String,
    modifier: Modifier = Modifier,
    titleInitial: String? = null,
    cornerRadius: Dp = 10.dp,
    iconSize: Dp = 26.dp
) {
    val colors = AvalonTheme.colors
    val avatarStyle = rememberAvatarStyle(lorebookId)
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .clip(shape)
            .background(colors.cardBackground)
            .border(1.dp, colors.strokeSubtle, shape),
        contentAlignment = Alignment.Center
    ) {
        if (!coverUrl.isNullOrBlank()) {
            ShimmerAsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = coverUrl,
                seed = lorebookId,
                contentScale = ContentScale.Crop,
                blurhash = null
            )
        } else {
            // Stylized Procedural Grimoire Cover
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(avatarStyle.backgroundBrush)
            ) {
                // Spine line effect on the left edge (book crease)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(4.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.5f),
                                    Color.White.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Center icon / grimoire emblem
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = IconsManager.SystemInstruction,
                        contentDescription = null,
                        tint = colors.neonAccent.copy(alpha = 0.85f),
                        modifier = Modifier.size(iconSize)
                    )
                }

                // Subtle inner border / glow
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            1.dp,
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.2f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.4f)
                                )
                            ),
                            shape
                        )
                )
            }
        }
    }
}
