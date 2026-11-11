package com.ensodai.shared.avalon.upload

class UploadFile(
    val paramName: String,
    val fileName: String,
    val contentType: String,
    val size: Long,
    val bytesProvider: () -> ByteArray
)
