package app.domain.character.usecases

import org.koin.core.annotation.Factory
import app.data.db.tables.aiCharacter.AICharacters
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import app.data.db.tables.aiCharacter.CharacterWelcomeCard
import app.data.db.tables.chatSessions.ChatSessions
import app.data.db.tables.images.CharacterAvatars
import app.data.db.tables.images.CharacterImages
import app.data.db.tables.images.WelcomeCardImages
import app.domain.utils.ErrorType
import app.utils.dbQuery
import app.service.chat.MessageNotifier
import com.ensodai.shared.avalon.chat.wss.ServerMessage
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Factory
class DeleteCharacterUseCase(
    private val messageNotifier: MessageNotifier
) {
    suspend fun execute(characterId: Uuid, userId: Uuid, sessionId: Uuid?) {
        dbQuery {
            val characterRow = AICharacters
                .select(AICharacters.owner, AICharacters.deletedAt)
                .where { AICharacters.id eq characterId }
                .singleOrNull()

            if (characterRow == null) return@dbQuery

            val ownerId = characterRow[AICharacters.owner]
            if (ownerId.value != userId) {
                throw Exception("You do not own this character")
            }

            if (characterRow[AICharacters.deletedAt] != null) {
                return@dbQuery
            }

            val now = Clock.System.now()

            AICharacters.update({ AICharacters.id eq characterId }) {
                it[deletedAt] = now
                it[visibility] = CharacterVisibility.PRIVATE
            }
            CharacterAvatars.update({ CharacterAvatars.characterPublicId eq characterId }) {
                it[deletedAt] = now
            }
            CharacterImages.update({ CharacterImages.characterPublicId eq characterId }) {
                it[deletedAt] = now
            }
            CharacterWelcomeCard.update({ CharacterWelcomeCard.character eq characterId }) {
                it[deletedAt] = now
            }
            WelcomeCardImages.update({ WelcomeCardImages.characterPublicId eq characterId }) {
                it[deletedAt] = now
            }
            ChatSessions.update({
                (ChatSessions.characterId eq characterId) and (ChatSessions.userId eq userId)
            }) {
                it[deletedAt] = now
            }
        }

        messageNotifier.notifyUserWithMessage(
            userId = userId,
            message = ServerMessage.DeleteCharacter(characterId),
            excludeSessionId = sessionId
        )
    }
}
