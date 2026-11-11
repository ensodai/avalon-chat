package com.ensodai.avalon.utils

class IosFileSaver : FileSaver {

    override suspend fun saveFile(
        bytes: ByteArray,
        fileName: String,
        mimeType: String
    ): Result<String> {
        return Result.success(fileName)
    }
}
