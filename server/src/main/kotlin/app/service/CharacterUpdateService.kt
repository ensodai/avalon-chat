package app.service

import app.data.network.models.request.UpdateCharacterRequest
import app.data.repository.CharacterRepository
import app.data.repository.UserRepositoryApp
import kotlinx.coroutines.CoroutineScope
import java.io.File
import kotlin.time.Instant
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single

// DTO для передачи данных в сервис
data class CharacterUpdateCommand(
    val userId: Uuid,
    val characterPublicId: Uuid,
    val characterData: UpdateCharacterRequest?,
    val avatarPublicId: Uuid?,
    val deleteImagesIds: List<String>,
    val tempAvatarFile: File?,
    val tempImagesFiles: List<File>,
    val tempWelcomeCardFiles: List<File>
)

@Single
class CharacterUpdateService(
    private val characterRepository: CharacterRepository,
    private val userRepository: UserRepositoryApp,
    private val imageSaver: ImageSaver,
    private val applicationScope: CoroutineScope
) {
    /**
     * Оркестрирует полный процесс обновления персонажа.
     */
    suspend fun update(command: CharacterUpdateCommand): Instant? {
        // --- Шаг 1: Работа с файловой системой ---
        val pathsOfDeletedFiles = characterRepository.deleteImageRecordsAndGetPaths(command.deleteImagesIds)

        // Сохраняем новые файлы и получаем их пути
        val newAvatarPath = command.tempAvatarFile?.let { imageSaver.saveImageAndGetPath(it, command.characterPublicId) }?.path
        val newImagePaths = command.tempImagesFiles.map { imageSaver.saveImageAndGetPath(it, command.characterPublicId).path }
        val newWelcomeCardPaths = command.tempWelcomeCardFiles.map { imageSaver.saveImageAndGetPath(it, command.characterPublicId).path }

        // --- Шаг 2: Работа с БД ---
        val isUserShadowBanned = userRepository.isUserShadowBanned(command.userId)

        val updatedAt = characterRepository.performCharacterUpdate(
            characterPublicId = command.characterPublicId,
            characterData = command.characterData,
            isUserShadowBanned = isUserShadowBanned,
            newAvatarFromGalleryId = command.avatarPublicId,
            newAvatarFromFilePath = newAvatarPath,
            newImagePaths = newImagePaths,
            newWelcomeCardPaths = newWelcomeCardPaths
        )

        return updatedAt

        // --- Шаг 3: Физическое удаление старых файлов ---
        // отключено
//        applicationScope.launch(Dispatchers.IO) {
//            pathsOfDeletedFiles.forEach { path ->
//                try {
//                    File(path).delete()
//                } catch (e: Exception) {
//                    logger.error("Failed to delete file: $path", e)
//                }
//            }
//        }
    }
}