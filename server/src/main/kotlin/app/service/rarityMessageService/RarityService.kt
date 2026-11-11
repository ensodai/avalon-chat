package app.service.rarityMessageService

import com.ensodai.shared.avalon.chat.models.*

import app.data.db.tables.users.AppUser
import app.data.db.tables.users.UserPityCounter
import app.data.db.tables.users.UserPityCounters
import app.utils.dbQuery
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import kotlin.random.Random
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single

/**
 * Сервис для определения редкости сообщения для пользователя,
 * использующий таблицу в базе данных для хранения счетчиков "жалости".
 */
/**
 * Сервис для определения редкости сообщения для пользователя,
 * использующий базу данных для хранения счетчиков "жалости" (pity counters).
 */
@Single
class RarityService {

    /**
     * Атомарно определяет следующую редкость для пользователя и обновляет его счетчик "жалости".
     *
     * @param userId UUID пользователя.
     * @return Вычисленная редкость [Rarity].
     */
    suspend fun determineNextRarityForUser(userId: Uuid): Rarity {
        return dbQuery { // Вся операция - одна атомарная транзакция
            // --- Шаг 1: Получаем профиль редкости пользователя ---
            // Используем DAO для получения пользователя и его профиля
            val appUser = AppUser.findById(userId)
            val rarityProfileName = appUser?.rarityProfileName ?: "CLASSIC_RPG"
            val rarityProfile = runCatching { RarityProfile.valueOf(rarityProfileName.uppercase()) }.getOrNull()
                ?: RarityProfile.CLASSIC_RPG

            // --- Шаг 2: Находим или создаем счетчик "жалости" для этого профиля ---
            val counter = UserPityCounter.find {
                (UserPityCounters.userId eq userId) and (UserPityCounters.profileName eq rarityProfile.name)
            }.singleOrNull() ?: UserPityCounter.new {
                // `id` (первичный ключ) сгенерируется автоматически
                this.userId = appUser!!.id // Безопасно, т.к. user не может быть null на этом этапе
                this.profileName = rarityProfile.name
                this.pityCount = 0
            }
            val currentPityCount = counter.pityCount

            // --- Шаг 3: Определяем редкость (бизнес-логика без изменений) ---
            val determinedRarity = if (currentPityCount >= rarityProfile.pityThreshold) {
                rarityProfile.pityReward
            } else {
                val chance = Random.nextDouble(0.0, 100.0)
                rarityProfile.getRarityForChance(chance)
            }

            // --- Шаг 4: Вычисляем новый счетчик (бизнес-логика без изменений) ---
            val newPityCount = if (determinedRarity.isCommonOrUncommon()) {
                currentPityCount + 1
            } else {
                0 // Сбрасываем счетчик при получении редкой награды
            }

            // --- Шаг 5: Обновляем свойство у нашего DAO-объекта ---
            // `lastUpdatedAt` обновится автоматически благодаря `defaultExpression(CurrentDateTime)`
            counter.pityCount = newPityCount

            // --- Шаг 6: Возвращаем результат ---
            determinedRarity
        }
    }
}

// Вспомогательная extension-функция для чистоты кода
private fun Rarity.isCommonOrUncommon(): Boolean {
    return this == Rarity.COMMON || this == Rarity.UNCOMMON
}