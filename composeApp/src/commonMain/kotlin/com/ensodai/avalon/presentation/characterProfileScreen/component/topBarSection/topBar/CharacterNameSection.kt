package com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.topBar

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.configs.ImageLayoutState
import com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.configs.ToolbarConfig
import com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.configs.calculateCollapsedTextRightPadding
import com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.configs.calculateColumnOffset
import com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.configs.calculateFontSize
import com.ensodai.avalon.presentation.chatScreen.component.topBar.ChatStatusHeader
import com.ensodai.avalon.presentation.core.ChatStatus
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun CharacterNameSection(
    currentHeight: Dp,
    imageLayoutState: ImageLayoutState,
    toolbarConfig: ToolbarConfig,
    characterName: String,
    status: ChatStatus
) {
    val colors = AvalonTheme.colors
    val density = LocalDensity.current
    var columnHeightPx by remember { mutableStateOf(0) }
    val columnHeightDp = with(density) { columnHeightPx.toDp() }

    val animationState = rememberCharacterNameSectionState(
        currentHeight = currentHeight,
        imageLayoutState = imageLayoutState,
        toolbarConfig = toolbarConfig,
        columnHeightDp = columnHeightDp
    )

    val textColor by animateColorAsState(
        targetValue = if (imageLayoutState.isExpanded) Color.White else colors.textMain,
        label = "nameTextColor"
    )

    Column(
        modifier = Modifier
            .padding(
                start = animationState.animatedOffsetXForColumn,
                top = animationState.animatedOffsetYForColumn
            )
            .padding(end = animationState.collapsedTextRightPadding)
            .onGloballyPositioned { coords -> columnHeightPx = coords.size.height }
    ) {
        Text(
            text = characterName,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Bold,
            fontSize = animationState.animatedCharacterNameFontSize
        )
        ChatStatusHeader(status)
    }
}


@Stable
data class CharacterNameSectionAnimationState(
    val animatedCharacterNameFontSize: TextUnit,
    val animatedOnlineStatusFontSize: TextUnit,
    val animatedOffsetXForColumn: Dp,
    val animatedOffsetYForColumn: Dp,
    val collapsedTextRightPadding: Dp
)

@Composable
fun rememberCharacterNameSectionState(
    currentHeight: Dp,
    imageLayoutState: ImageLayoutState,
    toolbarConfig: ToolbarConfig,
    columnHeightDp: Dp
): CharacterNameSectionAnimationState {

    val targetFontSize = calculateFontSize(currentHeight, toolbarConfig)
    val collapsedTextRightPadding = calculateCollapsedTextRightPadding(currentHeight, toolbarConfig)
    val animatedCharacterNameFontSize by animateFloatAsState(
        targetValue = targetFontSize.characterNameFontSize.value,
        label = "animatedCharacterNameFontSize"
    )
    val animatedOnlineStatusFontSize by animateFloatAsState(
        targetValue = targetFontSize.onlineStatusFontSize.value,
        label = "animatedOnlineStatusFontSize"
    )

    val (targetOffsetXForColumn, targetOffsetYForColumn) =
        calculateColumnOffset(currentHeight, imageLayoutState, columnHeightDp)

    val animatedOffsetXForColumn by animateDpAsState(
        targetValue = targetOffsetXForColumn,
        animationSpec = spring(stiffness = 2000f),
        label = "animatedOffsetXForColumn"
    )
    val animatedOffsetYForColumn by animateDpAsState(
        targetValue = targetOffsetYForColumn,
        animationSpec = spring(stiffness = 2000f),
        label = "animatedOffsetYForColumn"
    )

    return CharacterNameSectionAnimationState(
        animatedCharacterNameFontSize = animatedCharacterNameFontSize.sp,
        animatedOnlineStatusFontSize = animatedOnlineStatusFontSize.sp,
        animatedOffsetXForColumn = animatedOffsetXForColumn,
        animatedOffsetYForColumn = animatedOffsetYForColumn,
        collapsedTextRightPadding = collapsedTextRightPadding
    )
}