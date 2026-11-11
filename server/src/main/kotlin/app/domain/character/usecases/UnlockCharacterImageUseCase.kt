package app.domain.character.usecases

import org.koin.core.annotation.Factory
import app.data.db.tables.images.CharacterImages
import app.data.db.tables.images.CharacterImage
import app.data.db.tables.images.UnlockedCharacterImages
import app.data.db.tables.images.UnlockedCharacterImage
import app.data.db.tables.images.toResponse
import app.data.db.tables.users.AppUsers
import app.data.repository.UserRepository
import app.data.repository.CharacterRepository
import app.data.models.EnergyDeductionResult
import app.utils.dbQuery
import io.ktor.server.plugins.NotFoundException
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.select
import kotlin.uuid.Uuid

@Factory
class UnlockCharacterImageUseCase(
    private val userRepository: UserRepository,
    private val characterRepository: CharacterRepository
) {
    suspend fun execute(characterImageId: Uuid, userId: Uuid): com.ensodai.shared.avalon.character.models.UnlockCharacterImageResult {
        return dbQuery {
            // 1. Ищем изображение
            val image = CharacterImage.findById(characterImageId)
                ?: throw NotFoundException("Изображение с ID $characterImageId не найдено")

            val characterId = image.characterPublicId.value
            val isOwner = characterRepository.belongsToUser(characterId, userId)

            // 2. Проверяем, не открыто ли уже изображение
            val isAlreadyUnlocked = UnlockedCharacterImages
                .select(UnlockedCharacterImages.id)
                .where { (UnlockedCharacterImages.userId eq userId) and (UnlockedCharacterImages.characterImageId eq characterImageId) }
                .count() > 0

            if (isOwner) {
                return@dbQuery com.ensodai.shared.avalon.character.models.UnlockCharacterImageResult.Success(image.toResponse(isUnlocked = true))
            }

            if (image.requiredLevel != null) {
                throw IllegalArgumentException("Это изображение разблокируется автоматически при достижении ${image.requiredLevel} уровня отношений")
            }

            if (!image.isPrivate) {
                return@dbQuery com.ensodai.shared.avalon.character.models.UnlockCharacterImageResult.Success(image.toResponse(isUnlocked = true))
            }

            if (isAlreadyUnlocked) {
                return@dbQuery com.ensodai.shared.avalon.character.models.UnlockCharacterImageResult.Success(image.toResponse(isUnlocked = true))
            }

            // 3. Списание энергии
            val cost = image.energyCost
            val deductionResult = userRepository.deductEnergyForPhotoUnlock(
                userId = userId,
                cost = cost,
                characterImageId = characterImageId,
                characterId = characterId
            )

            when (deductionResult) {
                                is EnergyDeductionResult.RouteError -> {
                    throw IllegalStateException("RouteError not supported in this flow yet")
                }
                EnergyDeductionResult.UserNotFound -> {
                    throw NotFoundException("Пользователь не найден")
                }
                is EnergyDeductionResult.InsufficientFunds -> {
                    return@dbQuery com.ensodai.shared.avalon.character.models.UnlockCharacterImageResult.InsufficientEnergy(
                        requiredEnergy = cost,
                        currentEnergy = deductionResult.currentBalance.dailyEnergy + deductionResult.currentBalance.reserveEnergy
                    )
                }
                is EnergyDeductionResult.Success -> {
                    // Успешно списано!
                }
            }

            // 4. Записываем разблокировку в БД
            UnlockedCharacterImage.new {
                this.userId = EntityID(userId, AppUsers)
                this.characterImageId = EntityID(characterImageId, CharacterImages)
            }

            com.ensodai.shared.avalon.character.models.UnlockCharacterImageResult.Success(image.toResponse(isUnlocked = true))
        }
    }
}

