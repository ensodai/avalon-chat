package com.ensodai.avalon.utils

class WebFileSaver : FileSaver {

    override suspend fun saveFile(
        bytes: ByteArray,
        fileName: String,
        mimeType: String
    ): Result<String> {
        return Result.success(fileName)
    }
}
