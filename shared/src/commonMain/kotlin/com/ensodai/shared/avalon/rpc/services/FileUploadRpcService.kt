package com.ensodai.shared.avalon.rpc.services

import com.ensodai.shared.avalon.upload.UploadFileResponse
import kotlinx.rpc.annotations.Rpc

@Rpc
interface FileUploadRpcService {
    suspend fun uploadImage(
        type: String,
        fileName: String,
        content: ByteArray
    ): UploadFileResponse
}
