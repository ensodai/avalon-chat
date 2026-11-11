package app.service.relationship

import app.data.db.tables.users.UserCharacterRelationships
import com.ensodai.shared.avalon.relationship.RelationshipLevelCalculator
import app.service.chat.MessageNotifier
import app.utils.dbQuery
import app.utils.logger.WssLogger
import com.ensodai.shared.avalon.chat.wss.ServerMessage
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class CharacterRelationshipService(
    private val messageNotifier: MessageNotifier
) {
    /**
     * Добавляет опыт к отношениям пользователя и персонажа.
     * Если опыта достаточно для перехода на следующий уровень, повышает уровень
     * и отправляет WebSocket-событие.
     */
    suspend fun addExperience(userId: Uuid, characterId: Uuid, expAmount: Long) {
        if (expAmount <= 0) return

        dbQuery {
            // Ищем существующую запись
            val row = UserCharacterRelationships
                .select(
                    UserCharacterRelationships.relationshipLevel,
                    UserCharacterRelationships.experiencePoints
                )
                .where {
                    (UserCharacterRelationships.userId eq userId) and (UserCharacterRelationships.characterId eq characterId)
                }
                .forUpdate()
                .singleOrNull()

            if (row == null) {
                // Создаем новую запись, если её нет
                var newLevel = 1
                var currentExp = expAmount

                while (true) {
                    val reqForNext = RelationshipLevelCalculator.getExperienceRequiredForNextLevel(newLevel)
                    if (reqForNext in 1..currentExp) {
                        currentExp -= reqForNext
                        newLevel++
                    } else {
                        break
                    }
                }

                val maxEventLevel = (newLevel / 5) * 5
                val pendingLevel = if (maxEventLevel >= 5) maxEventLevel else null

                UserCharacterRelationships.insert {
                    it[this.id] = Uuid.random()
                    it[this.userId] = userId
                    it[this.characterId] = characterId
                    it[this.relationshipLevel] = newLevel
                    it[this.experiencePoints] = currentExp
                    it[this.pendingEventLevel] = pendingLevel
                }

                if (newLevel > 1) {
                    notifyLevelUp(userId, characterId, 1, newLevel, currentExp)
                }
                
                WssLogger.info("Created relationship for user $userId and char $characterId. Level: $newLevel, Exp: $currentExp")
            } else {
                // Обновляем существующую
                var currentLevel = row[UserCharacterRelationships.relationshipLevel]
                var currentExp = row[UserCharacterRelationships.experiencePoints] + expAmount
                val initialLevel = currentLevel

                while (true) {
                    val reqForNext = RelationshipLevelCalculator.getExperienceRequiredForNextLevel(currentLevel)
                    if (reqForNext in 1..currentExp) {
                        currentExp -= reqForNext
                        currentLevel++
                    } else {
                        break
                    }
                }

                val maxEventLevel = (currentLevel / 5) * 5
                val initialMaxEventLevel = (initialLevel / 5) * 5
                val newPendingLevel = if (maxEventLevel > initialMaxEventLevel && maxEventLevel >= 5) maxEventLevel else null

                UserCharacterRelationships.update({
                    (UserCharacterRelationships.userId eq userId) and (UserCharacterRelationships.characterId eq characterId)
                }) {
                    it[relationshipLevel] = currentLevel
                    it[experiencePoints] = currentExp
                    if (newPendingLevel != null) {
                        it[pendingEventLevel] = newPendingLevel
                    }
                }

                if (currentLevel > initialLevel) {
                    notifyLevelUp(userId, characterId, initialLevel, currentLevel, currentExp)
                    WssLogger.info("Relationship Level Up! user $userId, char $characterId. Level: $initialLevel -> $currentLevel")
                }
            }
        }
    }

    private suspend fun notifyLevelUp(userId: Uuid, characterId: Uuid, oldLevel: Int, newLevel: Int, currentExp: Long) {
        val reqForNext = RelationshipLevelCalculator.getExperienceRequiredForNextLevel(newLevel)
        messageNotifier.notifyUserWithMessage(
            userId = userId,
            message = ServerMessage.RelationshipLevelUp(
                characterId = characterId,
                oldLevel = oldLevel,
                newLevel = newLevel,
                currentExperience = currentExp,
                maxExperienceForNextLevel = reqForNext
            ),
            excludeSessionId = null
        )
    }
    suspend fun getRelationshipLevel(userId: Uuid, characterId: Uuid): Int {
        return dbQuery {
            UserCharacterRelationships
                .select(UserCharacterRelationships.relationshipLevel)
                .where {
                    (UserCharacterRelationships.userId eq userId) and (UserCharacterRelationships.characterId eq characterId)
                }
                .singleOrNull()
                ?.let { it[UserCharacterRelationships.relationshipLevel] } ?: 1
        }
    }

    suspend fun clearPendingEvent(userId: Uuid, characterId: Uuid) {
        dbQuery {
            UserCharacterRelationships.update({
                (UserCharacterRelationships.userId eq userId) and (UserCharacterRelationships.characterId eq characterId)
            }) {
                it[pendingEventLevel] = null
            }
        }
    }
}