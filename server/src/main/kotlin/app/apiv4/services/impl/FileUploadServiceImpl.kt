package app.apiv4.services.impl

import com.ensodai.shared.avalon.rpc.services.FileUploadRpcService
import com.ensodai.shared.avalon.upload.UploadFileResponse
import app.apiv4.infrastructure.BaseRpcService
import app.apiv4.infrastructure.RpcSessionContext
import app.service.ImageSaver
import app.cache.RedisService
import app.utils.toServerUrl
import io.ktor.server.application.ApplicationCall
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import java.io.File
import kotlin.uuid.Uuid

@Factory
class FileUploadServiceImpl(
    @InjectedParam private val call: ApplicationCall,
    @InjectedParam session: RpcSessionContext,
    private val imageSaver: ImageSaver
) : BaseRpcService(session), FileUploadRpcService {

    override suspend fun uploadImage(
        type: String,
        fileName: String,
        content: ByteArray
    ): UploadFileResponse {
        ensureAuthorized()

        val tempFile = File.createTempFile("upload_", "_$fileName")
        tempFile.writeBytes(content)

        try {
            // Сохраняем как "временную" картинку персонажа.
            val result = imageSaver.saveUniqueImage(tempFile, Uuid.random())

            // Сохраняем в Redis
            val token = RedisService.saveTempImage(result)

            return UploadFileResponse(
                token = token,
                imageUrl = result.path.toServerUrl(),
                width = result.width,
                height = result.height,
                thumbHash = result.thumbHash,
                sizeBytes = result.sizeBytes
            )
        } finally {
            if (tempFile.exists()) {
                tempFile.delete()
            }
        }
    }
}
