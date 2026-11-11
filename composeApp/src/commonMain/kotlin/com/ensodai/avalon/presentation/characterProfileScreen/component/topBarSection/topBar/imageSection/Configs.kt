package com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.topBar.imageSection

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.Dp
import com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.configs.ImageLayoutState

@Stable
data class ImageLayoutAnimationState(
    val imagePaddingStartAnim: Dp,
    val imagePaddingTopAnim: Dp,
    val imageSizeAnim: Dp,
    val cornerRadiusAnim: Dp
)

@Composable
fun rememberImageLayoutAnimationState(imageLayoutState: ImageLayoutState): ImageLayoutAnimationState {
    val imagePaddingStartAnim by animateDpAsState(
        targetValue = imageLayoutState.imagePaddingStart,
        label = "imagePaddingStartAnim"
    )
    val imagePaddingTopAnim by animateDpAsState(
        targetValue = imageLayoutState.imagePaddingTop,
        label = "imagePaddingTopAnim"
    )
    val imageSizeAnim by animateDpAsState(
        targetValue = imageLayoutState.imageSize,
        label = "imageSizeAnim"
    )
    val cornerRadiusAnim by animateDpAsState(
        targetValue = imageLayoutState.cornerRadius,
        label = "cornerRadiusAnim"
    )

    return ImageLayoutAnimationState(
        imagePaddingStartAnim = imagePaddingStartAnim,
        imagePaddingTopAnim = imagePaddingTopAnim,
        imageSizeAnim = imageSizeAnim,
        cornerRadiusAnim = cornerRadiusAnim
    )
}
