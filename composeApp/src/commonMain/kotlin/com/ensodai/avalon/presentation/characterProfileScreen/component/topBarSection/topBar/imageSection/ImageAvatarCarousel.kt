package com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.topBar.imageSection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.presentation.characterProfileScreen.viewState.CharacterAvatarWithSeed
import com.ensodai.avalon.presentation.commonComponents.ShimmerAsyncImage

@Composable
fun ImageAvatarCarousel(
    pagerState: PagerState,
    animationState: ImageLayoutAnimationState,
    characterAvatars: List<CharacterAvatarWithSeed>,
    userScrollEnabled: Boolean,
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .padding(
                start = animationState.imagePaddingStartAnim,
                top = animationState.imagePaddingTopAnim
            )
            .size(animationState.imageSizeAnim)
            .clip(RoundedCornerShape(animationState.cornerRadiusAnim)),
        userScrollEnabled = userScrollEnabled
    ) { page ->
        characterAvatars[page]

        val avatar = when {
            !userScrollEnabled -> characterAvatars.first()
            else -> characterAvatars[page]
        }
        ShimmerAsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = avatar.imageUrl,
            seed = avatar.seed,
            contentDescription = "avatar",
            contentScale = if (characterAvatars.isEmpty()) ContentScale.Fit else ContentScale.Crop,
            blurhash = avatar.thumbHash
        )
    }
    Box(modifier = Modifier.size(animationState.imageSizeAnim)) {
        TopBottomTint(isEnable = userScrollEnabled)
    }
}

@Composable
private fun TopBottomTint(
    isEnable: Boolean
) {
    if (isEnable) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Нижний скрим: всегда чёрный — обеспечивает контраст для имени персонажа
            // поверх любой аватарки (светлой или тёмной). Color.Black — intentional, не из темы.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.55f)
                            )
                        )
                    )
            )

            // Верхний скрим: для кнопок навигации
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}
