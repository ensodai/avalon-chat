package app.apiv4.routes

import app.cache.RedisService
import app.plugins.JWT_AUTH
import app.plugins.USER_ID_CLAIM
import com.ensodai.shared.avalon.upload.UploadFileResponse
import app.service.ImageSaveResult
import app.service.ImageSaver
import app.utils.toServerUrl
import app.utils.logger.EndpointLogger
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import org.koin.ktor.ext.inject
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun Route.uploadRoute() {
    val imageSaver: ImageSaver by inject()

    authenticate(JWT_AUTH) {
        post("/api/v3/upload") {
            val jwtPrincipal = call.principal<JWTPrincipal>()
            val userIdStr = jwtPrincipal?.payload?.getClaim(USER_ID_CLAIM)?.asString()

            if (userIdStr == null) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid token claims")
                return@post
            }

            val userId = try {
                Uuid.parse(userIdStr)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid user ID format")
                return@post
            }

            if (!call.request.contentType().match(ContentType.MultiPart.Any)) {
                call.respond(HttpStatusCode.BadRequest, "Expected multipart/form-data")
                return@post
            }

            val multipart = try {
                call.receiveMultipart()
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Multipart read failed")
                return@post
            }

            var uploadType: String? = null
            var savedFileResult: ImageSaveResult? = null

            try {
                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            if (part.name == "type") {
                                uploadType = part.value
                                call.application.environment.log.info(
                                    "uploadRoute: received upload type=$uploadType"
                                )
                            }
                            part.dispose()
                        }
                        is PartData.FileItem -> {
                            val resolvedUploadType = uploadType
                            if (resolvedUploadType == null) {
                                part.dispose()
                                throw IllegalArgumentException("Missing upload type")
                            }

                            call.application.environment.log.info(
                                "uploadRoute: received file part name=${part.name}, originalFileName=${part.originalFileName}"
                            )

                            savedFileResult = saveFile(part, userId, resolvedUploadType, imageSaver)
                            part.dispose()
                        }
                        else -> part.dispose()
                    }
                }

                val result = savedFileResult

                if (result == null) {
                    call.respond(HttpStatusCode.BadRequest, "Missing file")
                    return@post
                }

                if (uploadType == null) {
                    call.respond(HttpStatusCode.BadRequest, "Missing upload type")
                    return@post
                }

                // Сохраняем метаданные в Redis и получаем токен
                val token = RedisService.saveTempImage(result)

                // Возвращаем типизированный ответ
                call.respond(
                    HttpStatusCode.OK,
                    UploadFileResponse(
                        token = token,
                        imageUrl = result.path.toServerUrl(),
                        width = result.width,
                        height = result.height,
                        thumbHash = result.thumbHash,
                        sizeBytes = result.sizeBytes
                    )
                )

            } catch (e: IllegalArgumentException) {
                call.application.environment.log.warn("uploadRoute: bad request ${e.message}", e)
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.application.environment.log.error("uploadRoute: upload failed", e)
                call.respond(HttpStatusCode.InternalServerError, "Upload failed: ${e.message}")
            }
        }
    }
}

private suspend fun saveFile(
    filePart: PartData.FileItem,
    userId: Uuid,
    uploadType: String,
    imageSaver: ImageSaver
): ImageSaveResult {
    val originalName = filePart.originalFileName ?: "upload"
    val ext = File(originalName).extension.ifEmpty { "tmp" }
    val tempFile = File.createTempFile("upload_${Uuid.random()}_", ".$ext")

    try {
        EndpointLogger.request("/api/v3/upload", "POST")

        // Сохраняем во временный файл
        val readChannel = filePart.provider()
        val writeChannel = tempFile.writeChannel()

        try {
            readChannel.copyAndClose(writeChannel)
        } finally {
            if (!writeChannel.isClosedForWrite) {
                writeChannel.flushAndClose()
            }
        }

        // Сохраняем через ImageSaver в зависимости от типа
        val result = when (uploadType.lowercase()) {
            "character" -> imageSaver.saveUniqueImage(tempFile, userId)
            "story",
            "persona" -> imageSaver.saveStoryImage(tempFile, userId)
            else -> {
                tempFile.delete()
                throw IllegalArgumentException("Invalid upload type: $uploadType")
            }
        }

        EndpointLogger.response("/api/v3/upload", 200)
        return result
    } finally {
        // Удаляем временный файл (ImageSaver уже скопировал в нужное место)
        if (tempFile.exists()) {
            tempFile.delete()
        }
    }
}
