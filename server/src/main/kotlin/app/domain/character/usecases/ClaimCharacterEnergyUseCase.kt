package app.domain.character.usecases

import app.data.db.tables.aiCharacter.AICharacter
import app.data.db.tables.aiCharacter.AICharacters
import app.data.db.tables.users.AppUser
import app.data.db.tables.users.AppUsers
import app.data.db.tables.users.EnergyOperationType
import app.data.db.tables.users.EnergyHistory
import app.utils.dbQuery
import com.ensodai.shared.avalon.character.models.ClaimEnergyResponse
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
class ClaimCharacterEnergyUseCase {

    suspend fun execute(userId: Uuid, characterId: Uuid): ClaimEnergyResponse {
        return dbQuery {
            // 1. Находим персонажа
            val character = AICharacter.findById(characterId)
                ?: throw IllegalArgumentException("Персонаж не найден")

            // 2. Проверяем права владельца
            if (character.owner.value != userId) {
                throw SecurityException("Вы не являетесь владельцем этого персонажа")
            }

            // 3. Вычисляем целую часть клейма
            val claimAmountDouble = character.unclaimedEnergy
            val toClaim = claimAmountDouble.toInt()

            if (toClaim <= 0) {
                throw IllegalArgumentException("Накопленная энергия слишком мала для сбора. Копим дальше! ⚡")
            }

            // 4. Находим создателя
            val creator = AppUser.findById(userId)
                ?: throw IllegalArgumentException("Пользователь не найден")

            val oldReserve = creator.reserveBalance
            val newReserve = oldReserve + toClaim
            val remainingUnclaimed = claimAmountDouble - toClaim

            // 5. Обновляем балансы в БД
            // Обновляем баланс создателя
            AppUsers.update({ AppUsers.id eq userId }) {
                it[reserveBalance] = newReserve
            }

            // Обновляем баланс персонажа
            AICharacters.update({ AICharacters.id eq characterId }) {
                it[unclaimedEnergy] = remainingUnclaimed
            }

            // 6. Логируем в историю с типом CLAIM_BONUS
            EnergyHistory.insert {
                it[user] = userId
                it[operationType] = EnergyOperationType.CLAIM_BONUS
                it[amount] = toClaim
                it[dailyBalanceAfter] = creator.currentQuotaLimit
                it[reserveBalanceAfter] = newReserve
                it[metaData] = "character:$characterId;claimed:$toClaim;left:$remainingUnclaimed"
                it[relatedMessageId] = characterId
            }

            ClaimEnergyResponse(
                characterId = characterId,
                claimedAmount = toClaim,
                remainingUnclaimedEnergy = remainingUnclaimed,
                userReserveBalance = newReserve
            )
        }
    }
}
