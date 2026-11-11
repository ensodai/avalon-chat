package com.ensodai.avalon.data.repository.remote.rpc

import com.ensodai.shared.avalon.rpc.services.FileUploadRpcService
import com.ensodai.shared.avalon.upload.UploadFileResponse
import com.ensodai.avalon.di.RpcConnectionManager
import kotlinx.rpc.withService

open class FileUploadRpcServiceRemoteImpl(
    private val manager: RpcConnectionManager,
    private val executor: RpcCallExecutor
) : FileUploadRpcService {

    open suspend fun getService(): FileUploadRpcService = manager.getActiveClient().withService<FileUploadRpcService>()

    override suspend fun uploadImage(
        type: String,
        fileName: String,
        content: ByteArray
    ): UploadFileResponse =
        executor.execute("uploadImage", getService = { getService() }) { uploadImage(type, fileName, content) }
}
