package app.service

import app.data.db.tables.aiCharacter.AICharacters
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import app.data.db.tables.users.AppUsers
import app.data.network.models.response.getCurrentWeekRange
import app.utils.dbQuery
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.select
import org.koin.core.annotation.Single

/**
 * Сервис для получения агрегированной информации о персонажах.
 */
@Single
class CharacterService {

    /**
     * Получает общее количество всех "хороших" (публичных, не забаненных) персонажей.
     *
     * @return Общее количество персонажей в виде [Int].
     */
    suspend fun getAllCharactersCount(): Int {
        // Вызываем приватный метод-помощник без дополнительных условий
        return getCharacterCount()
    }

    /**
     * Получает количество "хороших" персонажей, созданных на текущей неделе.
     *
     * @return Количество новых персонажей в виде [Int].
     */
    suspend fun getNewCharactersCount(): Int {
        // Формируем дополнительное условие для фильтрации по дате
        val (startOfWeek, endOfWeek) = getCurrentWeekRange()
        val newCondition = (AICharacters.createdAt greaterEq startOfWeek) and (AICharacters.createdAt lessEq endOfWeek)

        // Вызываем приватный метод-помощник с этим условием
        return getCharacterCount(additionalCondition = newCondition)
    }

    /**
     * Приватный метод-помощник, инкапсулирующий общую логику подсчета персонажей.
     *
     * @param additionalCondition Опциональное дополнительное условие для фильтрации.
     * @return Количество персонажей, удовлетворяющих всем условиям.
     */
    private suspend fun getCharacterCount(additionalCondition: Op<Boolean>? = null): Int {
        return dbQuery {
            // --- Формируем базовое условие, общее для всех подсчетов ---
            var finalCondition: Op<Boolean> = (AICharacters.visibility eq CharacterVisibility.PUBLIC) and
                    (AICharacters.isBanned eq false) and
                    (AppUsers.deletedAt.isNull()) and
                    (AppUsers.isBanned eq false) and
                    (AppUsers.isShadowBanned eq false) // ИСПРАВЛЕНО: здесь должен быть AND, а не OR

            // Если передано дополнительное условие, добавляем его
            additionalCondition?.let {
                finalCondition = finalCondition and it
            }

            AICharacters
                .join(AppUsers, JoinType.INNER, onColumn = AICharacters.owner, otherColumn = AppUsers.id)
                .select(AICharacters.id)
                .where { finalCondition }
                .count()
                .toInt()
        }
    }
}