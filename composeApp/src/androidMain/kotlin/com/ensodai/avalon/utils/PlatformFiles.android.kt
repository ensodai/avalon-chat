package com.ensodai.avalon.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.ensodai.shared.avalon.upload.UploadFile
import org.koin.core.context.GlobalContext
import java.io.ByteArrayOutputStream
import java.io.File

actual fun saveImageToInternalStorage(imageBytes: ByteArray, fileName: String): String? {
    return try {
        val context = GlobalContext.get().get<Context>()
        val normalizedFileName =
            if ('.' in fileName.substringAfterLast('/')) fileName else "$fileName.jpg"
        val file = File(context.filesDir, normalizedFileName)
        file.writeBytes(imageBytes)
        file.absolutePath
    } catch (e: Exception) {
        L.e("PlatformFiles", "Failed to save image: ${e.stackTraceToString()}")
        null
    }
}

actual fun deleteFile(path: String): Boolean {
    return try {
        val file = File(path)
        if (file.exists()) {
            file.delete()
        } else {
            true
        }
    } catch (e: Exception) {
        L.e("PlatformFiles", "Failed to delete file: $path, error: ${e.message}")
        false
    }
}

actual fun createUploadFile(path: String?, paramName: String): UploadFile? {
    if (path == null) {
        return null
    }

    val file = File(path)
    if (!file.exists()) {
        return null
    }

    return UploadFile(
        paramName = paramName,
        fileName = file.name,
        contentType = "image/jpeg",
        size = file.length(),
        bytesProvider = file::readBytes
    )
}

actual fun compressImageIfNeeded(bytes: ByteArray, maxDimension: Int): ByteArray {
    return try {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        
        val width = options.outWidth
        val height = options.outHeight
        
        if (width <= 0 || height <= 0) return bytes
        
        var targetWidth = width
        var targetHeight = height
        if (width > maxDimension || height > maxDimension) {
            if (width > height) {
                targetWidth = maxDimension
                targetHeight = (height * (maxDimension.toDouble() / width)).toInt()
            } else {
                targetHeight = maxDimension
                targetWidth = (width * (maxDimension.toDouble() / height)).toInt()
            }
        }
        
        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(width, height, targetWidth, targetHeight)
        }
        
        val decodedBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions) ?: return bytes
        
        val scaledBitmap = if (decodedBitmap.width > targetWidth || decodedBitmap.height > targetHeight) {
            Bitmap.createScaledBitmap(decodedBitmap, targetWidth, targetHeight, true)
        } else {
            decodedBitmap
        }
        
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        
        if (scaledBitmap != decodedBitmap) {
            scaledBitmap.recycle()
        }
        decodedBitmap.recycle()
        
        outputStream.toByteArray()
    } catch (e: Exception) {
        L.e("PlatformFiles", "Failed to compress image: ${e.stackTraceToString()}")
        bytes
    }
}

private fun calculateInSampleSize(width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int {
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

