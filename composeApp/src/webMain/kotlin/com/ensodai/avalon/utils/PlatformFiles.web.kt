package com.ensodai.avalon.utils

import com.ensodai.shared.avalon.upload.UploadFile

actual fun saveImageToInternalStorage(imageBytes: ByteArray, fileName: String): String? = null

actual fun deleteFile(path: String): Boolean = true

actual fun createUploadFile(path: String?, paramName: String): UploadFile? = null

actual fun compressImageIfNeeded(bytes: ByteArray, maxDimension: Int): ByteArray = bytes

