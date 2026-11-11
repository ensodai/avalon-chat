package com.ensodai.avalon.utils

import com.ensodai.shared.avalon.upload.UploadFile
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.*
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun saveImageToInternalStorage(imageBytes: ByteArray, fileName: String): String? {
    val fileManager = NSFileManager.defaultManager
    val urls = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
    val documentsDirectory = urls.first() as? NSURL ?: return null
    
    val normalizedFileName = if ('.' in fileName.substringAfterLast('/')) fileName else "$fileName.jpg"
    val fileUrl = documentsDirectory.URLByAppendingPathComponent(normalizedFileName) ?: return null
    
    val data = imageBytes.usePinned { pinned ->
        NSData.dataWithBytes(pinned.addressOf(0), imageBytes.size.toULong())
    }
    
    return if (data.writeToURL(fileUrl, true)) {
        fileUrl.path
    } else {
        null
    }
}

@OptIn(ExperimentalForeignApi::class)
actual fun deleteFile(path: String): Boolean {
    val fileManager = NSFileManager.defaultManager
    return if (fileManager.fileExistsAtPath(path)) {
        fileManager.removeItemAtPath(path, null)
    } else {
        true
    }
}

actual fun createUploadFile(path: String?, paramName: String): UploadFile? = null

actual fun compressImageIfNeeded(bytes: ByteArray, maxDimension: Int): ByteArray = bytes

