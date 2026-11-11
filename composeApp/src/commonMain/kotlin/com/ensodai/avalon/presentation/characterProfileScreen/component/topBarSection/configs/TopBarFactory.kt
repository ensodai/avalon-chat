package com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.configs

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp


@Composable
fun createToolbarConfig(
    startWithFullScreenImages: Boolean,
    topBarHasExpanded: Boolean,
): ToolbarConfig {

    val statusBarPaddingDp = WindowInsets.statusBars
        .only(WindowInsetsSides.Top)
        .asPaddingValues()
        .calculateTopPadding()

    val toolbarHeightConfig = ToolbarHeightConfig(
        collapsed = 66.dp + statusBarPaddingDp,
        halfExpanded = 150.dp + statusBarPaddingDp,
        editModeExpanded = 250.dp + statusBarPaddingDp,
        fullExpanded = 365.dp + statusBarPaddingDp,
        overFullExpanded = 460.dp + statusBarPaddingDp
    )

    val defaultOpenScreenHeight = when {
        topBarHasExpanded && startWithFullScreenImages -> toolbarHeightConfig.fullExpanded
        topBarHasExpanded -> toolbarHeightConfig.halfExpanded
        else -> toolbarHeightConfig.halfExpanded
    }

    val imageToolbarConfig = ImageToolbarConfig(
        // Значения для состояния COLLAPSED
        collapsedPaddingStart = 66.dp,
        collapsedImagePaddingTop = 8.dp + statusBarPaddingDp,
        collapsedImageSize = 48.dp,
        // Значения для состояния HALF_EXPANDED
        halfExpandedPaddingStart = 16.dp,
        halfExpandedImagePaddingTop = 70.dp + statusBarPaddingDp,
        halfExpandedImageSize = 66.dp,
        // Параметры для состояния FULL_EXPANDED
        fullExpandedPaddingStart = 16.dp,
        fullExpandedImagePaddingTop = 50.dp + statusBarPaddingDp,
        fullExpandedImageSize = 110.dp
    )

//    val toolbarState = if (isEnableEditMode) ToolbarState.EDIT_MODE else ToolbarState.HALF_EXPANDED

    return ToolbarConfig(
        defaultOpenScreenHeight = defaultOpenScreenHeight,
        toolbarHeightConfig = toolbarHeightConfig,
        image = imageToolbarConfig,
//        toolbarState = toolbarState
    )
}


fun getDragFactor(currentValue: Dp, toolbarHeightsConfig: ToolbarHeightConfig): Float {
    return when {
        currentValue < toolbarHeightsConfig.halfExpanded -> 1f
        // Между halfExpanded и fullExpanded – применяем 50% эффекта
        currentValue < toolbarHeightsConfig.fullExpanded -> 0.5f
        // При overscroll также 50%
        else -> 0.5f
    }
}

// Функция для определения целевого состояния при отпускании жеста
fun getTargetState(current: Dp, toolbarHeightsConfig: ToolbarHeightConfig): ToolbarState {
    // Порог между COLLAPSED и HALF_EXPANDED – среднее значение между ними
    val collapsedHalfMidpoint =
        (toolbarHeightsConfig.collapsed + toolbarHeightsConfig.halfExpanded) / 2

    // Порог между HALF_EXPANDED и FULL_EXPANDED – начало от halfExpanded плюс 30% разницы до fullExpanded
    val halfFullThreshold =
        toolbarHeightsConfig.halfExpanded + (toolbarHeightsConfig.fullExpanded - toolbarHeightsConfig.halfExpanded) * 0.5f

    return when {
        // Если высота меньше halfExpanded, выбираем между COLLAPSED и HALF_EXPANDED
        current < toolbarHeightsConfig.halfExpanded -> {
            ToolbarState.COLLAPSED
            if (current < collapsedHalfMidpoint)
                ToolbarState.COLLAPSED
            else
                ToolbarState.HALF_EXPANDED
        }
        // Если высота находится между halfExpanded и fullExpanded, выбираем между HALF_EXPANDED и FULL_EXPANDED
        current <= toolbarHeightsConfig.fullExpanded -> {
            if (current > halfFullThreshold)
                ToolbarState.FULL_EXPANDED
            else
                ToolbarState.HALF_EXPANDED
        }
        // Если высота превышает fullExpanded, считаем, что это overscroll
        else -> ToolbarState.OVER_FULL_EXPANDED
    }
}

// Функция вычисления параметров для изображения тулбара
fun calculateImageLayoutState(
    currentHeight: Dp,
    toolbarConfig: ToolbarConfig,
    parentWidth: Dp,
    parentHeight: Dp
): ImageLayoutState {
    val collapsed = toolbarConfig.toolbarHeightConfig.collapsed
    val halfExpanded = toolbarConfig.toolbarHeightConfig.halfExpanded
    val fullExpanded = toolbarConfig.toolbarHeightConfig.fullExpanded
    val halfFullThreshold = halfExpanded + (fullExpanded - halfExpanded) * 0.5f
    val isEnableChatButton = currentHeight >= toolbarConfig.toolbarHeightConfig.collapsed + 15.dp

    return when {
        currentHeight <= halfExpanded -> {
            // Интерполяция между COLLAPSED и HALF_EXPANDED
            val fraction =
                ((currentHeight - collapsed) / (halfExpanded - collapsed)).coerceIn(0f, 1f)
            val paddingStart = lerp(
                toolbarConfig.image.collapsedPaddingStart,
                toolbarConfig.image.halfExpandedPaddingStart,
                fraction
            )
            val paddingTop = lerp(
                toolbarConfig.image.collapsedImagePaddingTop,
                toolbarConfig.image.halfExpandedImagePaddingTop,
                fraction
            )
            val imageSize = lerp(
                toolbarConfig.image.collapsedImageSize,
                toolbarConfig.image.halfExpandedImageSize,
                fraction
            )
            ImageLayoutState(
                imagePaddingStart = paddingStart,
                imagePaddingTop = paddingTop,
                imageSize = imageSize,
                cornerRadius = imageSize / 2,
                isExpanded = false,
                isEnableChatButton = isEnableChatButton
            )
        }

        currentHeight <= halfFullThreshold -> {
            // Интерполяция между HALF_EXPANDED и FULL_EXPANDED
            val fraction =
                ((currentHeight - halfExpanded) / (halfFullThreshold - halfExpanded)).coerceIn(
                    0f,
                    1f
                )
            val paddingStart = lerp(
                toolbarConfig.image.halfExpandedPaddingStart,
                toolbarConfig.image.fullExpandedPaddingStart,
                fraction
            )
            val paddingTop = lerp(
                toolbarConfig.image.halfExpandedImagePaddingTop,
                toolbarConfig.image.fullExpandedImagePaddingTop,
                fraction
            )
            val imageSize = lerp(
                toolbarConfig.image.halfExpandedImageSize,
                toolbarConfig.image.fullExpandedImageSize,
                fraction
            )
            ImageLayoutState(
                imagePaddingStart = paddingStart,
                imagePaddingTop = paddingTop,
                imageSize = imageSize,
                cornerRadius = imageSize / 2,
                isExpanded = false,
                isEnableChatButton = isEnableChatButton
            )
        }

        else -> {
            // Состояние, когда изображение полностью расширено
            ImageLayoutState(
                imagePaddingStart = 0.dp,
                imagePaddingTop = 0.dp,
                // +30.dp для нормализации размера и плавной анимации увеличения при оверсвайпе
                imageSize = maxOf(parentWidth, parentHeight + 30.dp),
                cornerRadius = 0.dp,
                isExpanded = true,
                isEnableChatButton = isEnableChatButton
            )
        }
    }
}

// Функция вычисления анимированного размера шрифта
fun calculateFontSize(
    currentHeight: Dp,
    toolbarConfig: ToolbarConfig,
    fontSizeConfig: FontSizeConfig = FontSizeConfig()
): FontSizeState {
    val collapsed = toolbarConfig.toolbarHeightConfig.collapsed
    val fullExpanded = toolbarConfig.toolbarHeightConfig.fullExpanded
    val fraction = ((currentHeight - collapsed) / (fullExpanded - collapsed)).coerceIn(0f, 1f)

    val characterNameFontSize = lerp(
        fontSizeConfig.characterNameFontSizeStart,
        fontSizeConfig.characterNameFontSizeEnd,
        fraction
    )
    val onlineStatusFontSize = lerp(
        fontSizeConfig.onlineStatusFontSizeStart,
        fontSizeConfig.onlineStatusFontSizeEnd,
        fraction
    )

    return FontSizeState(
        characterNameFontSize = characterNameFontSize,
        onlineStatusFontSize = onlineStatusFontSize
    )
}

// паддинг справа длля текста когда топ бар свернут
fun calculateCollapsedTextRightPadding(
    currentHeight: Dp,
    toolbarConfig: ToolbarConfig,
): Dp {
    return if (currentHeight >= toolbarConfig.toolbarHeightConfig.collapsed + 35.dp) {
        0.1.dp
    } else {
        50.dp
    }
}


// Функция вычисления смещения колонки с текстом
fun calculateColumnOffset(
    currentHeight: Dp,
    imageState: ImageLayoutState,
    columnHeight: Dp
): Pair<Dp, Dp> {
    val imageVerticalCenter = imageState.imagePaddingTop + (imageState.imageSize / 2)
    val offsetX =
        if (imageState.isExpanded) 16.dp else imageState.imagePaddingStart + imageState.imageSize + 16.dp
    val offsetY = if (imageState.isExpanded) {
        currentHeight - columnHeight - 16.dp
    } else {
        imageVerticalCenter - (columnHeight / 2)
    }
    return Pair(offsetX, offsetY)
}
