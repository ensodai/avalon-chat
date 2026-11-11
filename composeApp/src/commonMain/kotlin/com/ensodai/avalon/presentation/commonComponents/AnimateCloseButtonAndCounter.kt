package com.ensodai.avalon.presentation.commonComponents

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.theme.IconsManager

@Composable
fun AnimateCloseButtonAndCounter(
    modifier: Modifier = Modifier,
    isEnable: Boolean,
    onCloseClick: () -> Unit,
    isEnableCounter: Boolean = true,
    counter: Int
) {

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BaseItemScaleAnimation(isEnable) {
            IconButton(
                onClick = { onCloseClick() }
            ) {
                Icon(
                    imageVector = IconsManager.Close,
                    contentDescription = "Close select mode"
                )
            }
        }

        if (isEnableCounter) {
            AnimatedContent(
                targetState = if (counter == 0) 1 else counter,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInVertically { height -> height } + fadeIn()).togetherWith(
                            slideOutVertically { height -> -height } + fadeOut())
                    } else {
                        (slideInVertically { height -> -height } + fadeIn()).togetherWith(
                            slideOutVertically { height -> height } + fadeOut())
                    }.using(SizeTransform(clip = false))
                },
                label = "counter"
            ) { targetCounter ->
                BaseItemScaleAnimation(isEnable) {
                    Text(
                        text = targetCounter.toString(),
                        fontSize = 20.sp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
        }
    }
}

