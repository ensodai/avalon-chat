package com.ensodai.avalon.utils

import androidx.compose.runtime.Composable

interface ImageDownloader {
    fun downloadImage(url: String, fileName: String? = null)
}

@Composable
expect fun rememberImageDownloader(): ImageDownloader
