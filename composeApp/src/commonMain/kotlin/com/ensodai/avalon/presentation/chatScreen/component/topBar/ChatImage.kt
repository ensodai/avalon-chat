package com.ensodai.avalon.presentation.chatScreen.component.topBar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

@Composable
fun ChatImage(
    chatImage: String?,
    modifier: Modifier
) {
    AsyncImage(
        model = chatImage,
        contentDescription = "Chat Image",
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}
