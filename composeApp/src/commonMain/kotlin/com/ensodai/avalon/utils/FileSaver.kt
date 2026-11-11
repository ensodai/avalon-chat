package com.ensodai.avalon.utils

interface FileSaver {
    suspend fun saveFile(
        bytes: ByteArray,
        fileName: String,
        mimeType: String
    ): Result<String>
}
