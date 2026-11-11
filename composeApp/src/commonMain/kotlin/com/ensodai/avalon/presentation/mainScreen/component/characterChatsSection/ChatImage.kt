package com.ensodai.avalon.presentation.mainScreen.component.characterChatsSection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.presentation.commonComponents.AnimateCircleCheck
import com.ensodai.avalon.presentation.commonComponents.ShimmerAsyncImage
import com.ensodai.avalon.presentation.commonComponents.rememberAvatarStyle

@Composable
fun ChatImage(
    isSelected: Boolean,
    chatImage: String?,
    avatarSeed: String
) {

    val avatarStyle = rememberAvatarStyle(avatarSeed)

    Box(
        modifier = Modifier.size(52.dp)
    ) {

        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .background(avatarStyle.backgroundBrush),
            contentAlignment = Alignment.Center
        ) {
            ShimmerAsyncImage(
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape),
                model = chatImage,
                seed = avatarSeed,
                emojiSize = 26.dp,
                blurhash = null
            )
        }

        AnimateCircleCheck(
            isSelected = isSelected,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 5.dp, y = 5.dp)
        )
    }
}