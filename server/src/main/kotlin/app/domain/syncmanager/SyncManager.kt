package app.domain.syncmanager

import com.ensodai.shared.avalon.sync.models.SyncResponse
import app.service.LlmConfigService
import app.service.chat.MessageSlotSyncService
import app.utils.dbQuery
import com.ensodai.shared.avalon.sync.models.LlmConfigResponse
import com.ensodai.shared.avalon.auth.models.AppUserResponse
import com.ensodai.shared.avalon.persona.models.PersonaResponse
import com.ensodai.shared.avalon.character.models.CharacterResponse
import com.ensodai.shared.avalon.chat.models.ChatSessionPreviewResponse
import com.ensodai.shared.avalon.chat.models.ChatVariantResponse
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single


data class SyncEntityChanges<T>(
    val updatedItems: List<T>,
    val deletedIds: List<Uuid>
)

data class SyncDataRequest(
    val userId: Uuid,
    val sinceTimestamp: Instant
)

@Single
class SyncManager(
    private val userProfileService: UserProfileSyncService,
    private val personaService: PersonaSyncService,
    private val characterService: CharacterSyncService,
    private val chatSessionService: ChatSessionSyncService,
    private val chatVariantService: ChatVariantSyncService,
    private val messageSlotService: MessageSlotSyncService,
    private val llmConfigService: LlmConfigService
) {

    suspend fun execute(request: SyncDataRequest): SyncResponse {
        val (userId, sinceTimestamp) = request

        return dbQuery {

            // 1. Загружаем чаты
            val chatChanges = chatSessionService.fetchChanges(userId, sinceTimestamp)

            val characterIdsFromSyncedSessions = chatChanges.updatedItems
                .map { it.characterId}
                .distinct()

            // 2. Загружаем персонажей
            val characterChanges = characterService.fetchChanges(
                userId = userId,
                since = sinceTimestamp,
                forcedCharacterIds = characterIdsFromSyncedSessions
            )

            // 3. Загружаем профиль
            val appUser = userProfileService.fetchUser(userId, sinceTimestamp)

            // 4. Загружаем персоны
            val personaChanges = personaService.fetchChanges(userId, sinceTimestamp)

            // 5. Загружаем варианты чатов
            val variantChanges = chatVariantService.fetchChanges(userId, sinceTimestamp)

            val deletedSlots = messageSlotService.fetchDeletedSlots(userId, sinceTimestamp)

            val availableLlm = llmConfigService.getAvailableModels().map { model ->
                LlmConfigResponse(
                    codeName = model.code,
                    displayName = model.name,
                    price = model.energyCost
                )
            }

            // 6. Собираем ответ
            SyncResponse(
                updatedAppUser = appUser,
                updatedUserPersonas = personaChanges.updatedItems,
                deletedUserPersonaIds = personaChanges.deletedIds,

                updatedCharacters = characterChanges.updatedItems,
                deletedCharacterIds = characterChanges.deletedIds,

                updatedChatSessions = chatChanges.updatedItems,
                deletedChatSessionIds = chatChanges.deletedIds,

                updatedChatVariants = variantChanges.updatedItems,
                deletedChatVariantIds = variantChanges.deletedIds,

                latestTimestamp = Clock.System.now(),

                deletedSlots = deletedSlots,
                availableLlm = availableLlm
            )
        }
    }
}
