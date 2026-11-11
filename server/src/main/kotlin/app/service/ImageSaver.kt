package app.service

import app.service.image.ImagePreviewGenerator
import io.ktor.http.content.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import javax.imageio.ImageIO
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single

data class ImageSaveResult(
    val path: String,
    val width: Int,
    val height: Int,
    val thumbHash: String,
    val sizeBytes: Long
)

@Single
class ImageSaver {

    companion object {
        const val MAX_IMAGE_SIZE_BYTES = 10L * 1024 * 1024 // 10 MB
    }

    /**
     * Универсальный метод для обработки уже сохраненного файла.
     * Читает картинку, узнает размеры, генерирует ThumbHash и возвращает готовый результат.
     */
    private fun processSavedFile(file: File): ImageSaveResult {
        if (file.length() > MAX_IMAGE_SIZE_BYTES) {
            file.delete() // Удаляем слишком большой файл
            throw IllegalArgumentException("Размер файла превышает допустимый лимит (10 МБ).")
        }

        val image: BufferedImage = ImageIO.read(file)
            ?: throw IllegalArgumentException("Не удалось декодировать картинку: ${file.name}")

        // Защита от гигантских разрешений (например, 20000x20000), которые весят мало (если это однотонный PNG), 
        // но при распаковке в оперативную память для ThumbHash могут повесить сервер (OutOfMemoryError).
        if (image.width > 4000 || image.height > 4000) {
            file.delete()
            throw IllegalArgumentException("Разрешение картинки слишком большое (максимум 4000x4000).")
        }

        val thumbHash = ImagePreviewGenerator.generateThumbHashString(image)

        return ImageSaveResult(
            path = file.absolutePath,
            width = image.width,
            height = image.height,
            thumbHash = thumbHash,
            sizeBytes = file.length()
        )
    }

    private fun getOrCreateFolder(subPath: String): File {
        var folder = File("/var/uploads/$subPath")
        if (!folder.exists()) {
            val created = folder.mkdirs()
            if (!created) {
                // Если не получилось создать в /var/ (нет прав при локальном запуске),
                // "не стесняемся" и создаем папку прямо в корне проекта
                folder = File("uploads/$subPath")
                folder.mkdirs()
            }
        }
        return folder
    }

    suspend fun saveUniqueImage(
        tempFile: File,
        characterPublicId: Uuid
    ): ImageSaveResult = withContext(Dispatchers.IO) {
        val folder = getOrCreateFolder(characterPublicId.toString())
        val uniqueName = "image_${Uuid.random()}.jpg"
        val destinationFile = File(folder, uniqueName)

        tempFile.copyTo(destinationFile, overwrite = true)

        return@withContext processSavedFile(destinationFile)
    }

    suspend fun saveAvatarBytes(
        bytes: ByteArray,
        characterPublicId: Uuid,
        extension: String = "png"
    ): ImageSaveResult = withContext(Dispatchers.IO) {
        val folder = getOrCreateFolder(characterPublicId.toString())
        val destinationFile = File(folder, "avatar_${Uuid.random()}.$extension")

        if (bytes.size > MAX_IMAGE_SIZE_BYTES) {
            throw IllegalArgumentException("Размер изображения аватара превышает допустимый лимит (10 МБ).")
        }

        destinationFile.writeBytes(bytes)
        return@withContext processSavedFile(destinationFile)
    }

    suspend fun saveImageAndGetPath(
        part: PartData.FileItem,
        characterPublicId: Uuid,
        fileName: String = "image_${Uuid.random()}.jpg"
    ): ImageSaveResult = withContext(Dispatchers.IO) {
        val folder = getOrCreateFolder(characterPublicId.toString())
        val file = File(folder, fileName)

        val readChannel = part.provider()
        val writeChannel = file.writeChannel()

        try {
            readChannel.copyAndClose(writeChannel)
        } finally {
            if (!writeChannel.isClosedForWrite) {
                writeChannel.flushAndClose()
            }
        }

        return@withContext processSavedFile(file)
    }

    suspend fun saveImageAndGetPath(
        tempFile: File,
        characterPublicId: Uuid
    ): ImageSaveResult = withContext(Dispatchers.IO) {
        val folder = getOrCreateFolder(characterPublicId.toString())
        val fileName = tempFile.name
        val destFile = File(folder, fileName)

        tempFile.copyTo(destFile, overwrite = true)

        return@withContext processSavedFile(destFile)
    }

    suspend fun saveStoryImage(
        tempFile: File,
        userId: Uuid
    ): ImageSaveResult = withContext(Dispatchers.IO) {
        val folder = getOrCreateFolder("users/user_$userId/story_images")
        val newFileName = "${Uuid.random()}.jpg"
        val destFile = File(folder, newFileName)

        tempFile.copyTo(destFile, overwrite = true)

        return@withContext processSavedFile(destFile)
    }

    suspend fun saveGeneratedImage(
        bytes: ByteArray,
        userId: Uuid
    ): ImageSaveResult = withContext(Dispatchers.IO) {
        val folder = getOrCreateFolder("users/user_$userId/generated")
        val destFile = File(folder, "gen_${Uuid.random()}.jpg")

        if (bytes.size > MAX_IMAGE_SIZE_BYTES) {
            throw IllegalArgumentException("Размер сгенерированной картинки превышает 10 МБ")
        }

        // Пишем байты на диск
        destFile.writeBytes(bytes)

        // Для сгенерированной картинки мы можем прочитать её прямо из памяти (bytes),
        // это чуть быстрее, чем читать сохраненный файл с диска, как в processSavedFile.
        val image = ImageIO.read(ByteArrayInputStream(bytes))
            ?: throw IllegalArgumentException("Не удалось декодировать сгенерированную картинку")

        if (image.width > 4000 || image.height > 4000) {
            destFile.delete()
            throw IllegalArgumentException("Разрешение картинки слишком большое (максимум 4000x4000).")
        }

        val thumbHashString = ImagePreviewGenerator.generateThumbHashString(image)

        return@withContext ImageSaveResult(
            path = destFile.absolutePath,
            width = image.width,
            height = image.height,
            thumbHash = thumbHashString,
            sizeBytes = bytes.size.toLong()
        )
    }

    suspend fun deleteImage(imagePath: String?): Boolean = withContext(Dispatchers.IO) {
        if (imagePath.isNullOrBlank()) return@withContext true
        try {
            val fileToDelete = File(imagePath)
            if (!fileToDelete.exists()) return@withContext true
            return@withContext fileToDelete.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
}