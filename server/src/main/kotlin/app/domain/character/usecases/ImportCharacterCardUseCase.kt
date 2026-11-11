@file:OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)

package app.domain.character.usecases

import app.data.CategoryFactory
import app.data.db.tables.aiCharacter.AICharacterData
import app.data.repository.CharacterCreationRepository
import app.data.repository.UserRepositoryApp
import app.service.ImageSaveResult
import app.service.ImageSaver
import com.ensodai.shared.avalon.character.models.CharacterAlternateGreetingDto
import com.ensodai.shared.avalon.character.models.CharacterLorebookEntryInput
import com.ensodai.shared.avalon.character.models.CharacterLorebookInput
import com.ensodai.shared.avalon.character.models.CharacterPromptDetailsInput
import com.ensodai.shared.avalon.character.models.CharacterPublishedResponse
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.shared.avalon.character.models.ImportCharacterCardRequest
import com.ensodai.shared.avalon.character.models.InteractionFormat
import com.ensodai.shared.avalon.character.models.LorebookEntryPosition
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import com.ensodai.shared.avalon.character.spec.CharacterCardData
import com.ensodai.shared.avalon.character.spec.CharacterCardParser
import com.ensodai.shared.avalon.character.spec.ParsedCharacterCard
import com.ensodai.shared.avalon.character.spec.PngCharacterCardCodec
import org.koin.core.annotation.Factory
import org.slf4j.LoggerFactory
import kotlin.io.encoding.Base64
import kotlin.uuid.Uuid

@Factory
class ImportCharacterCardUseCase(
    private val characterCreationRepository: CharacterCreationRepository,
    private val userRepository: UserRepositoryApp,
    private val imageSaver: ImageSaver
) {
    private val logger = LoggerFactory.getLogger(ImportCharacterCardUseCase::class.java)

    suspend fun execute(request: ImportCharacterCardRequest, userId: Uuid): CharacterPublishedResponse {
        val rawBase64 = request.fileBase64.substringAfter("base64,")
        val fileBytes = runCatching {
            Base64.Default.decode(rawBase64)
        }.getOrElse {
            throw IllegalArgumentException("Не удалось декодировать Base64 содержимое файла: ${it.message}")
        }

        val characterPublicId = Uuid.random()
        val isPng = PngCharacterCardCodec.isPng(fileBytes)

        val parsedCard: ParsedCharacterCard = if (isPng) {
            PngCharacterCardCodec.extractCard(fileBytes)
                ?: throw IllegalArgumentException("В PNG файле не найдены метаданные карточки персонажа (чанки ccv3 или chara)")
        } else {
            val jsonString = runCatching { fileBytes.decodeToString() }.getOrElse {
                throw IllegalArgumentException("Файл не является валидным UTF-8 текстом или PNG изображением")
            }
            runCatching {
                CharacterCardParser.parse(jsonString)
            }.getOrElse {
                throw IllegalArgumentException("Не удалось распознать формат карточки персонажа: ${it.message}")
            }
        }

        val cardData: CharacterCardData = parsedCard.data

        val isShadowBanned = userRepository.isUserShadowBanned(userId)
        val finalVisibility = if (isShadowBanned) {
            CharacterVisibility.PRIVATE
        } else {
            request.visibility
        }

        val avatarResult: ImageSaveResult? = if (isPng) {
            runCatching {
                imageSaver.saveAvatarBytes(fileBytes, characterPublicId, extension = "png")
            }.onFailure {
                logger.warn("Не удалось сохранить аватар из PNG карточки: ${it.message}", it)
            }.getOrNull()
        } else {
            null
        }

        val name = cardData.name.ifBlank { "Импортированный персонаж" }
        val shortDesc = cardData.description.ifBlank {
            cardData.personality.take(200).ifBlank { "Карточка $name" }
        }
        val fullDesc = cardData.personality.ifBlank {
            cardData.description.ifBlank { "Импортированный персонаж $name" }
        }
        val firstMessage = cardData.firstMes.ifBlank { "Привет!" }

        val promptDetails = CharacterPromptDetailsInput(
            scenario = cardData.scenario.ifBlank { null },
            mesExample = cardData.mesExample.ifBlank { null },
            customSystemPrompt = cardData.systemPrompt.ifBlank { null },
            postHistoryInstructions = cardData.postHistoryInstructions.ifBlank { null },
            creator = cardData.creator.ifBlank { null },
            characterVersion = cardData.characterVersion.ifBlank { null },
            creatorNotes = cardData.creatorNotes.ifBlank { null },
            isImported = true // Карточка импортирована -> блокируем редактирование промпта
        )

        val alternateGreetings = cardData.alternateGreetings
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val tags = cardData.tags
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val lorebook = cardData.characterBook?.let { book ->
            CharacterLorebookInput(
                name = book.name?.ifBlank { null } ?: "Лорбук $name",
                description = book.description,
                scanDepth = book.scanDepth ?: 5,
                tokenBudget = book.tokenBudget,
                recursiveScanning = book.recursiveScanning ?: false,
                entries = book.entries.map { entry ->
                    CharacterLorebookEntryInput(
                        keys = entry.keys,
                        secondaryKeys = entry.secondaryKeys,
                        content = entry.content,
                        comment = entry.comment ?: entry.name,
                        enabled = entry.enabled,
                        constant = entry.constant ?: false,
                        selective = entry.selective ?: false,
                        insertionOrder = entry.insertionOrder,
                        position = when (entry.position?.lowercase()) {
                            "after_char" -> LorebookEntryPosition.AFTER_CHAR
                            "before_desc" -> LorebookEntryPosition.BEFORE_DESC
                            "after_desc" -> LorebookEntryPosition.AFTER_DESC
                            "personality" -> LorebookEntryPosition.PERSONALITY
                            "scenario" -> LorebookEntryPosition.SCENARIO
                            "at_depth", "depth" -> LorebookEntryPosition.AT_DEPTH
                            else -> LorebookEntryPosition.BEFORE_CHAR
                        },
                        depth = entry.depth ?: 4,
                        useRegex = entry.useRegex ?: false
                    )
                }
            )
        }

        val aiCharacterData = AICharacterData(
            publicId = characterPublicId,
            owner = userId,
            characterName = name,
            shortDescription = shortDesc,
            fullDescription = fullDesc,
            temperature = 0.85f,
            visibility = finalVisibility,
            isPublicFullDescription = true,
            category = CategoryFactory.getDefaultCategory().categoryCode,
            firstMessage = firstMessage,
            welcomeCard = null,
            isOutreachEnabled = false,
            voice = VoiceModel.ZEPHYR,
            interactionFormat = InteractionFormat.LITERARY_RPG,
            promptDetails = promptDetails,
            alternateGreetings = alternateGreetings,
            tags = tags,
            lorebook = lorebook
        )

        val creationResult = characterCreationRepository.createCharacterInTransaction(
            AICharacterData = aiCharacterData,
            publicIdCharacter = characterPublicId,
            characterImages = null,
            characterAvatar = avatarResult,
            welcomeCardImages = null,
            events = null,
            userId = userId,
            newImagesWithSpecs = null
        )

        return CharacterPublishedResponse(
            character = creationResult.characterResponse,
            chatSession = creationResult.chatSessionResponse,
            chatVariant = creationResult.firstChatVariant
        )
    }
}
