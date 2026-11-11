package com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.configs

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ToolbarConfig(
    val defaultOpenScreenHeight: Dp,
    val toolbarHeightConfig: ToolbarHeightConfig = ToolbarHeightConfig(),
    val image: ImageToolbarConfig = ImageToolbarConfig(),
)

data class ToolbarHeightConfig(
    val collapsed: Dp = 56.dp,
    val halfExpanded: Dp = 150.dp,
    val editModeExpanded: Dp = 250.dp,
    val fullExpanded: Dp = 365.dp,
    val overFullExpanded: Dp = 460.dp,
)

// состояния
enum class ToolbarState {
    COLLAPSED,
    HALF_EXPANDED,
    FULL_EXPANDED,
    OVER_FULL_EXPANDED
}

data class ImageToolbarConfig(
    // Значения для состояния COLLAPSED
    val collapsedPaddingStart: Dp = 50.dp,
    val collapsedImagePaddingTop: Dp = 4.dp,
    val collapsedImageSize: Dp = 50.dp,
    // Значения для состояния HALF_EXPANDED
    val halfExpandedPaddingStart: Dp = 16.dp,
    val halfExpandedImagePaddingTop: Dp = 50.dp,
    val halfExpandedImageSize: Dp = 66.dp,
    // Параметры для состояния FULL_EXPANDED
    val fullExpandedPaddingStart: Dp = 16.dp,
    val fullExpandedImagePaddingTop: Dp = 50.dp,
    val fullExpandedImageSize: Dp = 110.dp
)

// Data класс для вычисленных параметров изображения
data class ImageLayoutState(
    val imagePaddingStart: Dp,
    val imagePaddingTop: Dp,
    val imageSize: Dp,
    val cornerRadius: Dp,
    val isExpanded: Boolean,
    val isEnableChatButton: Boolean
)

data class FontSizeConfig(
    val characterNameFontSizeStart: TextUnit = 18.sp,
    val characterNameFontSizeEnd: TextUnit = 36.sp,
    val onlineStatusFontSizeStart: TextUnit = 16.sp,
    val onlineStatusFontSizeEnd: TextUnit = 24.sp
)

data class FontSizeState(
    val characterNameFontSize: TextUnit,
    val onlineStatusFontSize: TextUnit
)
