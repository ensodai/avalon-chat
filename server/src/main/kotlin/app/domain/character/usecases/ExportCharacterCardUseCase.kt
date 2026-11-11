@file:OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)

package app.domain.character.usecases

import app.data.db.tables.images.CharacterAvatars
import app.data.repository.CharacterRepository
import app.data.repository.CharacterSpecRepository
import app.domain.character.export.BackendCardImageRenderer
import app.utils.dbQuery
import app.utils.toServerUrl
import com.ensodai.shared.avalon.character.models.CharacterCardExportFormat
import com.ensodai.shared.avalon.character.models.CharacterLorebookDetailsDto
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.shared.avalon.character.models.ExportCharacterCardResponse
import com.ensodai.shared.avalon.character.models.LorebookEntryPosition
import com.ensodai.shared.avalon.character.spec.AvalonCardCustomization
import com.ensodai.shared.avalon.character.spec.CardImageRenderer
import com.ensodai.shared.avalon.character.spec.CharacterBookDto
import com.ensodai.shared.avalon.character.spec.CharacterBookEntryDto
import com.ensodai.shared.avalon.character.spec.CharacterCardData
import com.ensodai.shared.avalon.character.spec.CharacterCardEnvelope
import com.ensodai.shared.avalon.character.spec.PngCharacterCardCodec
import com.ensodai.shared.avalon.character.spec.characterCardJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.koin.core.annotation.Factory
import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import javax.imageio.ImageIO
import kotlin.io.encoding.Base64
import kotlin.uuid.Uuid

@Factory
class ExportCharacterCardUseCase(
    private val characterRepository: CharacterRepository,
    private val characterSpecRepository: CharacterSpecRepository,
    private val cardImageRenderer: CardImageRenderer = BackendCardImageRenderer()
) {

    suspend fun execute(characterId: Uuid, format: CharacterCardExportFormat, userId: Uuid): ExportCharacterCardResponse {
        val character = characterRepository.getCharacterById(characterId, userId)
        val isOwner = character.canEdit

        if (!isOwner && (character.visibility == CharacterVisibility.PRIVATE || !character.isPublicFullDescription)) {
            throw IllegalArgumentException("Экспорт персонажа с приватными полями запрещен")
        }

        val promptDetails = if (isOwner) characterSpecRepository.getPromptDetails(characterId) else null
        val greetings = characterSpecRepository.getAlternateGreetings(characterId)
        val tags = characterSpecRepository.getTags(characterId)
        val lorebook = if (isOwner) characterSpecRepository.getLorebook(characterId) else null

        val defaultCustomization = AvalonCardCustomization()
        val extensionsMap = buildJsonObject {
            put(AvalonCardCustomization.EXTENSION_KEY, defaultCustomization.toExtensionJson())
        }

        val cardData = CharacterCardData(
            name = character.characterName,
            description = character.shortDescription,
            personality = character.fullDescription ?: "",
            scenario = promptDetails?.scenario ?: "",
            firstMes = character.firstMessage ?: "",
            mesExample = promptDetails?.mesExample ?: "",
            creatorNotes = promptDetails?.creatorNotes ?: "",
            systemPrompt = promptDetails?.customSystemPrompt ?: "",
            postHistoryInstructions = promptDetails?.postHistoryInstructions ?: "",
            alternateGreetings = greetings.map { it.greetingText },
            tags = tags,
            creator = promptDetails?.creator ?: character.ownerNick,
            characterVersion = promptDetails?.characterVersion ?: "1.0",
            characterBook = lorebook?.toSpecDto(),
            extensions = extensionsMap
        )

        val isV3 = (format == CharacterCardExportFormat.PNG_V3 || format == CharacterCardExportFormat.JSON_V3)
        val envelope = CharacterCardEnvelope(
            spec = if (isV3) "chara_card_v3" else "chara_card_v2",
            specVersion = if (isV3) "3.0" else "2.0",
            data = characterCardJson.encodeToJsonElement(cardData).jsonObject
        )

        val envelopeJson = characterCardJson.encodeToString(CharacterCardEnvelope.serializer(), envelope)

        val safeName = character.characterName.replace(Regex("[^a-zA-Z0-9а-яА-Я._-]"), "_").take(50)

        return when (format) {
            CharacterCardExportFormat.JSON_V2, CharacterCardExportFormat.JSON_V3 -> {
                val base64Content = Base64.Default.encode(envelopeJson.encodeToByteArray())
                ExportCharacterCardResponse(
                    format = format,
                    fileBase64 = base64Content,
                    fileName = "${safeName}_card.json",
                    mimeType = "application/json"
                )
            }
            CharacterCardExportFormat.PNG_V3 -> {
                val rawAvatarBytes = loadAvatarRawBytes(characterId)
                val customization = AvalonCardCustomization.fromExtensions(cardData.extensions)
                val approxTokens = (cardData.name.length + cardData.description.length + cardData.personality.length + cardData.scenario.length + cardData.firstMes.length + cardData.systemPrompt.length) / 4
                val renderedPosterBytes = cardImageRenderer.renderCardToPng(
                    characterName = cardData.name,
                    nickname = cardData.nickname,
                    creatorNick = cardData.creator,
                    shortDescription = cardData.description,
                    tags = cardData.tags,
                    avatarBytes = rawAvatarBytes,
                    hasLorebook = !cardData.characterBook?.entries.isNullOrEmpty(),
                    versionText = if (cardData.characterVersion.isNotBlank()) "v${cardData.characterVersion.removePrefix("v")}" else "v1.0",
                    specVersion = "V3",
                    tokenCount = maxOf(approxTokens, 120),
                    altGreetingsCount = cardData.alternateGreetings.size,
                    customization = customization
                )
                val embeddedPng = PngCharacterCardCodec.embedCardJson(
                    originalPngBytes = renderedPosterBytes,
                    cardJson = envelopeJson,
                    keyword = "ccv3"
                )
                val base64Content = Base64.Default.encode(embeddedPng)
                ExportCharacterCardResponse(
                    format = format,
                    fileBase64 = base64Content,
                    fileName = "${safeName}_card.png",
                    mimeType = "image/png"
                )
            }
            CharacterCardExportFormat.PNG_V2 -> {
                val avatarBytes = loadAvatarPngBytes(characterId)
                val embeddedPng = PngCharacterCardCodec.embedCardJson(
                    originalPngBytes = avatarBytes,
                    cardJson = envelopeJson,
                    keyword = "chara"
                )
                val base64Content = Base64.Default.encode(embeddedPng)
                ExportCharacterCardResponse(
                    format = format,
                    fileBase64 = base64Content,
                    fileName = "${safeName}_card.png",
                    mimeType = "image/png"
                )
            }
        }
    }

    private suspend fun resolveAvatarBytes(characterId: Uuid): ByteArray? {
        val imagePath = dbQuery {
            CharacterAvatars.selectAll()
                .where {
                    (CharacterAvatars.characterPublicId eq characterId) and
                            (CharacterAvatars.deletedAt.isNull())
                }
                .orderBy(CharacterAvatars.createdAt to SortOrder.DESC)
                .firstOrNull()
                ?.get(CharacterAvatars.imagePath)
        } ?: return null

        if (imagePath.isBlank()) return null
        return resolveImageBytes(imagePath)
    }

    private suspend fun resolveImageBytes(imagePath: String): ByteArray? = withContext(Dispatchers.IO) {
        // 1. Проверяем локальный файл по прямому пути
        val directFile = File(imagePath)
        if (directFile.exists() && directFile.isFile) {
            return@withContext directFile.readBytes()
        }

        // 2. Проверяем относительный путь в каталоге uploads
        val uploadsIndex = imagePath.indexOf("uploads/")
        if (uploadsIndex != -1) {
            val relativeSub = imagePath.substring(uploadsIndex)
            val relFile = File(relativeSub)
            if (relFile.exists() && relFile.isFile) {
                return@withContext relFile.readBytes()
            }
        }

        // 3. Если файла на диске нет (локальная разработка с удаленной БД или CDN), скачиваем по сети
        val url = imagePath.toServerUrl()
        runCatching {
            val uri = URI(url)
            val connection = uri.toURL().openConnection() as HttpURLConnection
            connection.connectTimeout = 4000
            connection.readTimeout = 8000
            connection.requestMethod = "GET"
            connection.instanceFollowRedirects = true
            if (connection.responseCode in 200..299) {
                connection.inputStream.use { it.readBytes() }
            } else {
                null
            }
        }.getOrNull()
    }

    private suspend fun loadAvatarRawBytes(characterId: Uuid): ByteArray? {
        return resolveAvatarBytes(characterId)
    }

    private suspend fun loadAvatarPngBytes(characterId: Uuid): ByteArray {
        val rawBytes = resolveAvatarBytes(characterId)
        if (rawBytes != null && rawBytes.isNotEmpty()) {
            if (PngCharacterCardCodec.isPng(rawBytes)) {
                return rawBytes
            }
            val bufferedImage = runCatching {
                ByteArrayInputStream(rawBytes).use { ImageIO.read(it) }
            }.getOrNull()

            if (bufferedImage != null) {
                val baos = ByteArrayOutputStream()
                ImageIO.write(bufferedImage, "png", baos)
                return baos.toByteArray()
            }
        }

        return createFallbackAvatarPng()
    }

    private fun CharacterLorebookDetailsDto.toSpecDto(): CharacterBookDto {
        return CharacterBookDto(
            name = this.name,
            description = this.description,
            scanDepth = this.scanDepth,
            tokenBudget = this.tokenBudget,
            recursiveScanning = this.recursiveScanning,
            entries = this.entries.map { entry ->
                CharacterBookEntryDto(
                    keys = entry.keys,
                    content = entry.content,
                    enabled = entry.enabled,
                    insertionOrder = entry.insertionOrder,
                    comment = entry.comment,
                    selective = entry.selective,
                    secondaryKeys = entry.secondaryKeys,
                    constant = entry.constant,
                    position = if (entry.position == LorebookEntryPosition.BEFORE_CHAR) "before_char" else "after_char",
                    useRegex = entry.useRegex
                )
            }
        )
    }

    private fun createFallbackAvatarPng(): ByteArray {
        val width = 512
        val height = 512
        val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = img.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.color = Color(0x0C, 0x0D, 0x12)
        g.fillRect(0, 0, width, height)
        g.color = Color(0x00, 0xE5, 0xFF, 120)
        g.drawRect(16, 16, width - 32, height - 32)
        g.dispose()

        val baos = ByteArrayOutputStream()
        ImageIO.write(img, "png", baos)
        return baos.toByteArray()
    }
}
