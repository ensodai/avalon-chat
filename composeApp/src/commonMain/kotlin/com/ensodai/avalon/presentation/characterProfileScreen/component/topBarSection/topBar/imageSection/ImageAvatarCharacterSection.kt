package com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.topBar.imageSection

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.ensodai.shared.avalon.character.spec.AvalonCardCustomization
import com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.configs.ImageLayoutState
import com.ensodai.avalon.presentation.characterProfileScreen.viewState.CharacterAvatarWithSeed
import com.ensodai.avalon.presentation.commonComponents.BaseItemAlphaAnimation
import com.ensodai.avalon.presentation.commonComponents.SegmentedProgressBar
import com.ensodai.avalon.presentation.commonComponents.cardMonolith.AvalonMonolithCard
import com.ensodai.avalon.presentation.presentationUtils.statusBarPaddingDp

@Composable
fun ImageAvatarCharacterSection(
    characterAvatars: List<CharacterAvatarWithSeed>,
    imageLayoutState: ImageLayoutState,
    defaultSeed: String,
    isCardFlipped: Boolean = false,
    cardCustomization: AvalonCardCustomization = AvalonCardCustomization(),
    characterName: String = "",
    creatorNick: String = "",
    shortDescription: String = "",
    onToggleCardFlip: () -> Unit = {}
) {
    val animationState = rememberImageLayoutAnimationState(imageLayoutState = imageLayoutState)
    val avatar =
        characterAvatars.ifEmpty { listOf(CharacterAvatarWithSeed(id = null, imageUrl = null, seed = defaultSeed)) }.first()

    val pagerState = rememberPagerState(pageCount = { characterAvatars.size })
    LaunchedEffect(imageLayoutState.isExpanded) {
        if (!imageLayoutState.isExpanded) {
            pagerState.scrollToPage(0)
        }
    }

    val rotationY by animateFloatAsState(
        targetValue = if (isCardFlipped && imageLayoutState.isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "cardFlipRotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                this.rotationY = rotationY
                cameraDistance = 14f * density
            }
            .clickable(
                enabled = imageLayoutState.isExpanded,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onToggleCardFlip()
            }
    ) {
        if (rotationY <= 90f) {
            if (characterAvatars.size > 1) {
                ImageAvatarCarousel(
                    pagerState = pagerState,
                    animationState = animationState,
                    characterAvatars = characterAvatars,
                    userScrollEnabled = imageLayoutState.isExpanded
                )
            } else {
                SingleImageAvatar(
                    avatar = avatar,
                    pagerState = pagerState,
                    animationState = animationState
                )
            }
            BaseItemAlphaAnimation(isVisible = characterAvatars.isNotEmpty() && imageLayoutState.isExpanded && characterAvatars.size > 1) {
                SegmentedProgressBar(
                    modifier = Modifier.padding(top = statusBarPaddingDp()),
                    totalSteps = characterAvatars.size,
                    currentStep = pagerState.currentPage + 1,
                    segmentHeight = 2.dp
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = statusBarPaddingDp() + 8.dp, bottom = 12.dp)
                    .graphicsLayer {
                        this.rotationY = 180f
                    },
                contentAlignment = Alignment.Center
            ) {
                AvalonMonolithCard(
                    modifier = Modifier.fillMaxHeight(),
                    characterName = characterName,
                    creatorNick = creatorNick,
                    shortDescription = shortDescription,
                    avatarModel = avatar.imageUrl,
                    customization = cardCustomization,
                    specVersion = "V3"
                )
            }
        }
    }
}

