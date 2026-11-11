package app.data.repository

import app.data.db.tables.aiCharacter.AICharacter
import app.data.db.tables.aiCharacter.AICharacterData
import app.cache.RedisService
import app.data.db.tables.aiCharacter.CharacterWelcomeCardEntity
import app.data.db.tables.aiCharacter.toResponse
import app.data.db.tables.aiCharacter.AICharacterStats
import app.data.db.tables.aiCharacter.CharacterEvents
import app.data.db.tables.chatSessions.*
import app.data.db.tables.images.CharacterAvatar
import app.data.db.tables.images.CharacterImage
import app.data.db.tables.images.WelcomeCardImage
import app.data.db.tables.images.toResponse
import app.data.db.tables.users.AppUsers
import app.data.dto.CompleteCreationResult
import com.ensodai.shared.avalon.character.models.WelcomeCardRequest
import app.service.ImageSaveResult
import app.utils.dbQuery
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import app.data.db.tables.aiCharacter.AICharacterAlternateGreetings
import app.data.db.tables.aiCharacter.AICharacterLorebookEntries
import app.data.db.tables.aiCharacter.AICharacterLorebooks
import app.data.db.tables.aiCharacter.AICharacterPromptDetails
import app.data.db.tables.aiCharacter.AICharacterTags
import org.jetbrains.exposed.v1.dao.load
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single

data class NewImageWithSpec(
    val saveResult: app.service.ImageSaveResult,
    val isPrivate: Boolean = false,
    val energyCost: Int = 0,
    val requiredLevel: Int? = null
)

@Single
class CharacterCreationRepository(
    private val characterSpecRepository: CharacterSpecRepository
) {

    /**
     * Атомарно создает персонажа, все связанные с ним изображения, приветственную карточку
     * и сессию чата в рамках ОДНОЙ транзакции.
     *
     * @return FullCharacterCreationResult, содержащий DAO-сущности для формирования ответа.
     */
    suspend fun createCharacterInTransaction(
        AICharacterData: AICharacterData,
        publicIdCharacter: Uuid,
        characterImages: List<ImageSaveResult>?,
        characterAvatar: ImageSaveResult?,
        welcomeCardImages: List<ImageSaveResult>?,
        events: List<com.ensodai.shared.avalon.character.models.CharacterEventDto>?,
        userId: Uuid,
        newImagesWithSpecs: List<NewImageWithSpec>? = null
    ): CompleteCreationResult { // <-- Возвращает новый DTO
        return dbQuery {
            // --- Часть 1: Создание всех DAO-сущностей ---
            val newCharacter = createNewCharacter(publicIdCharacter, AICharacterData)
            
            // Создаем дефолтную запись статистики (0 сообщений)
            AICharacterStats.insert {
                it[this.characterId] = publicIdCharacter
                it[this.messageCount] = 0
                it[this.totalGeneratedTokens] = 0
                it[this.lastInteractionAt] = Clock.System.now()
            }

            val savedAvatarDao = characterAvatar?.let { saveAvatar(newCharacter, it) }
            val savedImagesDao = saveImages(newCharacter, characterImages, newImagesWithSpecs)
            val savedWelcomeCardImagesDao = AICharacterData.welcomeCard?.let {
                saveWelcomeCard(
                    character = newCharacter,
                    welcomeCardDto = it,
                    imageResults = welcomeCardImages
                )
            } ?: emptyList()

            events?.forEach { event ->
                CharacterEvents.insert {
                    it[this.characterId] = publicIdCharacter
                    it[this.requiredLevel] = event.requiredLevel
                    it[this.eventTriggerScenario] = event.eventTriggerScenario
                    it[this.eventPermanentTrait] = event.eventPermanentTrait
                }
            }

            AICharacterData.promptDetails?.let { details ->
                characterSpecRepository.savePromptDetails(publicIdCharacter, details)
            }

            if (AICharacterData.alternateGreetings.isNotEmpty()) {
                characterSpecRepository.saveAlternateGreetings(publicIdCharacter, AICharacterData.alternateGreetings)
            }

            if (AICharacterData.tags.isNotEmpty()) {
                characterSpecRepository.saveTags(publicIdCharacter, AICharacterData.tags)
            }

            AICharacterData.lorebook?.let { lb ->
                characterSpecRepository.saveLorebook(publicIdCharacter, lb)
            }

            if (AICharacterData.attachedLorebookIds.isNotEmpty()) {
                AICharacterData.attachedLorebookIds.forEach { lbId ->
                    runCatching {
                        characterSpecRepository.attachLorebook(
                            characterId = publicIdCharacter,
                            lorebookId = lbId,
                            userId = userId
                        )
                    }
                }
            }

            val (sessionDao, variantDao) = findOrCreateChatSessionAndVariant(userId, newCharacter)

            // --- Часть 2: Сборка Response DTO внутри транзакции ---

            // Явно загружаем связанные сущности, чтобы безопасно вызывать мапперы
            newCharacter.load(AICharacter::ownerEntity, AICharacter::avatar, AICharacter::images, AICharacter::welcomeCard, AICharacter::welcomeCardImages)
            variantDao.load(ChatVariant::chatSession, ChatVariant::chatVariantImage)

            // Инвалидируем кэш категорий в Redis
            RedisService.invalidateCategoryCache(AICharacterData.category)

            // Возвращаем итоговый объект со всеми готовыми частями
            CompleteCreationResult(
                characterResponse = newCharacter.toResponse(userId),
                chatSessionResponse = sessionDao.toPreviewResponse(),
                firstChatVariant = variantDao.toResponse()
            )
        }
    }


    private fun Transaction.findOrCreateChatSessionAndVariant(
        userId: Uuid,
        character: AICharacter
    ): Pair<ChatSession, ChatVariant> {

        // ШАГ 1: Пытаемся найти существующую запись (СУПЕР БЫСТРО ЧЕРЕЗ DSL)
        // Join нужен, чтобы убедиться, что вариант тоже существует
        val existingRow = (ChatSessions innerJoin ChatVariants)
            .selectAll()
            .where { (ChatSessions.userId eq userId) and (ChatSessions.characterId eq character.id) }
            .limit(1)
            .singleOrNull()

        if (existingRow != null) {
            // Если нашли — просто "оборачиваем" строку в Entity.
            // Это не делает новых запросов к БД.
            val session = ChatSession.wrapRow(existingRow)
            val variant = ChatVariant.wrapRow(existingRow)
            return session to variant
        }

        // ШАГ 2: Если не нашли — создаем всё вручную.
        // Генерируем ID заранее. Это позволяет нам легко разрулить связи.
        val newSessionId = Uuid.random()
        val newVariantId = Uuid.random()
        val now = Clock.System.now()
        // Или используй java.time.OffsetDateTime.now() если у тебя timestamps c TimeZone

        // 2.1. Вставляем Сессию (пока без activeVariantId, чтобы не было ошибки FK)
        ChatSessions.insert {
            it[id] = newSessionId
            it[this.userId] = userId
            it[this.characterId] = character.id.value // Берем чистый UUID
            it[isArchived] = false
            it[activeVariantId] = null // Пока null
        }

        // 2.2. Вставляем Вариант (ссылаемся на уже созданный ID сессии)
        ChatVariants.insert {
            it[id] = newVariantId
            it[chatSessionId] = newSessionId
            it[name] = character.characterName
        }

        // 2.3. Обновляем Сессию (проставляем activeVariantId)
        ChatSessions.update({ ChatSessions.id eq newSessionId }) {
            it[activeVariantId] = newVariantId
        }

        // ШАГ 3: Возвращаем результат
        // Нам нужно вернуть Entity объекты, так как остальной код их ждет.
        // Мы используем [id], что создает "прокси" объект.
        // Exposed не полезет в базу сразу, он запомнит ID.
        // Данные подгрузятся только когда ты вызовешь поля (lazy loading),
        // но так как мы только что их записали, это валидно.

        val newSession = ChatSession[newSessionId]
        val newVariant = ChatVariant[newVariantId]

        return newSession to newVariant
    }
    private fun Transaction.createNewCharacter(publicIdCharacter: Uuid, AICharacterData: AICharacterData): AICharacter {
        return AICharacter.new(publicIdCharacter) {
            this.owner = EntityID(AICharacterData.owner, AppUsers)
            this.geminiModel = "gemini-2.5-flash-preview-05-20"
            this.characterName = AICharacterData.characterName
            this.fullDescription = AICharacterData.fullDescription
            this.shortDescription = AICharacterData.shortDescription
            this.temperature = AICharacterData.temperature
            this.interactionFormat = AICharacterData.interactionFormat
            this.harassment = 0
            this.hate = 0
            this.sexuallyExplicit = 0
            this.dangerousContent = 0
            this.topK = 39
            this.topP = 0.95f
            this.maxOutputTokens = 8129
            this.maxContextTokens = 1000000
            this.visibility = AICharacterData.visibility
            this.isPublicFullDescription = AICharacterData.isPublicFullDescription
            this.category = AICharacterData.category
            this.firstMessage = AICharacterData.firstMessage
            this.isOutreachEnabled = AICharacterData.isOutreachEnabled
            this.voice = AICharacterData.voice
        }
    }

    private fun Transaction.saveAvatar(character: AICharacter, avatarResult: app.service.ImageSaveResult): CharacterAvatar {
        return CharacterAvatar.new {
            this.characterPublicId = character.id
            this.imagePath = avatarResult.path
            this.thumbHash = avatarResult.thumbHash
        }
    }

    private fun Transaction.saveImages(
        character: AICharacter,
        imageResults: List<ImageSaveResult>?,
        newImagesWithSpecs: List<NewImageWithSpec>?
    ): List<CharacterImage> {
        val resultList = mutableListOf<CharacterImage>()
        newImagesWithSpecs?.forEach { spec ->
            resultList.add(CharacterImage.new {
                this.characterPublicId = character.id
                this.imagePath = spec.saveResult.path
                this.thumbHash = spec.saveResult.thumbHash
                this.isPrivate = spec.isPrivate
                this.energyCost = spec.energyCost
                this.requiredLevel = spec.requiredLevel
            })
        }
        imageResults?.forEach { result ->
            resultList.add(CharacterImage.new {
                this.characterPublicId = character.id
                this.imagePath = result.path
                this.thumbHash = result.thumbHash
                this.isPrivate = false
                this.energyCost = 0
                this.requiredLevel = null
            })
        }
        return resultList
    }

    private fun Transaction.saveWelcomeCard(
        character: AICharacter,
        welcomeCardDto: WelcomeCardRequest,
        imageResults: List<app.service.ImageSaveResult>?
    ): List<WelcomeCardImage> {

        CharacterWelcomeCardEntity.new(character.id.value) {
            this.character = character
            this.welcomeCardDescription = welcomeCardDto.welcomeCardDescription
            this.cardColorStart = welcomeCardDto.cardColorStart
            this.cardColorEnd = welcomeCardDto.cardColorEnd
            this.cardColorLight = welcomeCardDto.cardColorLight
            this.isEnableLight = welcomeCardDto.isEnableLight
        }

        return imageResults?.mapIndexed { index, result ->
            WelcomeCardImage.new {
                this.characterPublicId = character.id
                this.imagePath = result.path
                this.thumbHash = result.thumbHash
                this.orderIndex = index
            }
        } ?: emptyList()
    }
}
