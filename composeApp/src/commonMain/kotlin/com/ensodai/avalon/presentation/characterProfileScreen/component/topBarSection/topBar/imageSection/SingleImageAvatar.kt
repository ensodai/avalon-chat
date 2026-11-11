package com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.topBar.imageSection

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.ensodai.avalon.presentation.characterProfileScreen.viewState.CharacterAvatarWithSeed
import com.ensodai.avalon.presentation.commonComponents.ShimmerAsyncImage

@Composable
fun SingleImageAvatar(
    avatar: CharacterAvatarWithSeed,
    pagerState: PagerState,
    animationState: ImageLayoutAnimationState
) {
    LaunchedEffect(pagerState) {
        pagerState.scrollToPage(0)
    }

    ShimmerAsyncImage(
        modifier = Modifier
            .padding(
                start = animationState.imagePaddingStartAnim,
                top = animationState.imagePaddingTopAnim
            )
            .size(animationState.imageSizeAnim)
            .clip(RoundedCornerShape(animationState.cornerRadiusAnim)),
        model = avatar.imageUrl,
        seed = avatar.seed,
        contentDescription = "avatar",
        blurhash = avatar.thumbHash
    )
}
