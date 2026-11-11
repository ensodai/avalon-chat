package com.ensodai.avalon.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

private class AndroidImageDownloader(
    private val context: Context
) : ImageDownloader {
    override fun downloadImage(url: String, fileName: String?) {
        val filename = fileName ?: "image_${System.currentTimeMillis()}.jpg"

        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Downloading image")
            .setDescription("Saving image to Downloads")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "AvalonAvatars/$filename"
            )
            .setMimeType("image/jpeg")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }
}

@Composable
actual fun rememberImageDownloader(): ImageDownloader {
    val context = LocalContext.current
    return remember(context) {
        AndroidImageDownloader(context)
    }
}
