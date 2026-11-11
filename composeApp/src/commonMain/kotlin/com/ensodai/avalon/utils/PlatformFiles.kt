package com.ensodai.avalon.utils

import com.ensodai.shared.avalon.upload.UploadFile

expect fun saveImageToInternalStorage(imageBytes: ByteArray, fileName: String): String?

expect fun deleteFile(path: String): Boolean

expect fun createUploadFile(path: String?, paramName: String): UploadFile?

expect fun compressImageIfNeeded(bytes: ByteArray, maxDimension: Int = 2048): ByteArray

