package com.ensodai.avalon.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

private object StubImageDownloader : ImageDownloader {
    override fun downloadImage(url: String, fileName: String?) = Unit
}

@Composable
actual fun rememberImageDownloader(): ImageDownloader {
    return remember { StubImageDownloader }
}
