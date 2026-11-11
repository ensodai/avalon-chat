package com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.configs.gestureExtention

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.configs.ToolbarConfig
import com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.configs.getDragFactor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun updateToolbarHeight(
    verticalDragAmount: Float,
    density: Density,
    toolbarHeightAnimatable: Animatable<Dp, AnimationVector1D>,
    toolbarConfig: ToolbarConfig,
    coroutineScope: CoroutineScope,
    isOnlyHalfExpanded: Boolean,
) {
    val dragDp = with(density) { verticalDragAmount.toDp() }
    val currentHeight = toolbarHeightAnimatable.value
    val dragFactor = getDragFactor(
        currentValue = currentHeight,
        toolbarHeightsConfig = toolbarConfig.toolbarHeightConfig
    )
    val effectiveDrag = dragDp * dragFactor
    val updatedHeight = (currentHeight + effectiveDrag).coerceIn(
        toolbarConfig.toolbarHeightConfig.collapsed,
        toolbarConfig.toolbarHeightConfig.overFullExpanded
    )
    val newHeight = if (isOnlyHalfExpanded) {
        if (updatedHeight >= toolbarConfig.toolbarHeightConfig.halfExpanded) {
            toolbarConfig.toolbarHeightConfig.halfExpanded
        } else {
            updatedHeight
        }
    } else {
        updatedHeight
    }
    coroutineScope.launch {
        toolbarHeightAnimatable.snapTo(newHeight)
    }
}
