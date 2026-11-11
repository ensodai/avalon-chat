package com.ensodai.shared.avalon.character.spec

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Стандартный кодек JSON для карточек персонажей с мягким парсингом.
 */
val characterCardJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
    prettyPrint = false
}

/**
 * Корневой конверт карточки персонажа V2 / V3.
 */
@Serializable
data class CharacterCardEnvelope(
    val spec: String? = null,
    @SerialName("spec_version")
    val specVersion: String? = null,
    val data: JsonObject? = null
)

/**
 * Данные карточки персонажа согласно спецификациям V2 и V3.
 */
@Serializable
data class CharacterCardData(
    val name: String = "",
    val description: String = "",
    val personality: String = "",
    val scenario: String = "",
    @SerialName("first_mes")
    val firstMes: String = "",
    @SerialName("mes_example")
    val mesExample: String = "",
    @SerialName("creator_notes")
    val creatorNotes: String = "",
    @SerialName("system_prompt")
    val systemPrompt: String = "",
    @SerialName("post_history_instructions")
    val postHistoryInstructions: String = "",
    @SerialName("alternate_greetings")
    val alternateGreetings: List<String> = emptyList(),
    @SerialName("character_book")
    val characterBook: CharacterBookDto? = null,
    val tags: List<String> = emptyList(),
    val creator: String = "",
    @SerialName("character_version")
    val characterVersion: String = "",
    val extensions: JsonObject = JsonObject(emptyMap()),

    // Дополнения спецификации V3
    val assets: List<CharacterAssetDto> = emptyList(),
    val nickname: String? = null,
    @SerialName("creator_notes_multilingual")
    val creatorNotesMultilingual: Map<String, String>? = null,
    val source: List<String> = emptyList(),
    @SerialName("group_only_greetings")
    val groupOnlyGreetings: List<String> = emptyList(),
    @SerialName("creation_date")
    val creationDate: Long? = null,
    @SerialName("modification_date")
    val modificationDate: Long? = null
)

/**
 * База знаний персонажа (Lorebook / World Info).
 */
@Serializable
data class CharacterBookDto(
    val name: String? = null,
    val description: String? = null,
    @SerialName("scan_depth")
    val scanDepth: Int? = null,
    @SerialName("token_budget")
    val tokenBudget: Int? = null,
    @SerialName("recursive_scanning")
    val recursiveScanning: Boolean? = null,
    val extensions: JsonObject = JsonObject(emptyMap()),
    val entries: List<CharacterBookEntryDto> = emptyList()
)

/**
 * Статья / запись базы знаний.
 */
@Serializable
data class CharacterBookEntryDto(
    val keys: List<String> = emptyList(),
    val content: String = "",
    val extensions: JsonObject = JsonObject(emptyMap()),
    val enabled: Boolean = true,
    @SerialName("insertion_order")
    val insertionOrder: Int = 0,
    @SerialName("case_sensitive")
    val caseSensitive: Boolean? = null,
    val name: String? = null,
    val priority: Int? = null,
    val id: JsonElement? = null,
    val comment: String? = null,
    val selective: Boolean? = null,
    @SerialName("secondary_keys")
    val secondaryKeys: List<String>? = null,
    val constant: Boolean? = null,
    val position: String? = null, // "before_char", "after_char", "before_desc", "after_desc", "personality", "scenario", "at_depth"
    val depth: Int? = null,
    @SerialName("use_regex")
    val useRegex: Boolean? = null
)

/**
 * Вспомогательный ассет (V3) - иконки, выражения лица, фоны.
 */
@Serializable
data class CharacterAssetDto(
    val type: String,
    val uri: String,
    val name: String,
    val ext: String = "png"
)

/**
 * Результат парсинга карточки персонажа с сохранением исходного JSON для round-trip целостности.
 */
data class ParsedCharacterCard(
    val specVersion: String, // "v1", "v2", "v3"
    val data: CharacterCardData,
    val rawJson: JsonObject
)

object CharacterCardParser {

    /**
     * Разбирает JSON карточки персонажа любой версии (V1, V2, V3).
     */
    fun parse(jsonString: String): ParsedCharacterCard {
        val rootElement = characterCardJson.parseToJsonElement(jsonString)
        val rootObject = rootElement.jsonObject

        val spec = rootObject["spec"]?.jsonPrimitive?.content
        val specVersion = rootObject["spec_version"]?.jsonPrimitive?.content

        return when {
            // V3
            spec == "chara_card_v3" || specVersion == "3.0" -> {
                val dataObj = rootObject["data"]?.jsonObject ?: rootObject
                val cardData = characterCardJson.decodeFromJsonElement<CharacterCardData>(dataObj)
                ParsedCharacterCard(
                    specVersion = "v3",
                    data = cardData,
                    rawJson = rootObject
                )
            }
            // V2
            spec == "chara_card_v2" || specVersion == "2.0" -> {
                val dataObj = rootObject["data"]?.jsonObject ?: rootObject
                val cardData = characterCardJson.decodeFromJsonElement<CharacterCardData>(dataObj)
                ParsedCharacterCard(
                    specVersion = "v2",
                    data = cardData,
                    rawJson = rootObject
                )
            }
            // V1 (legacy TavernAI - поля лежат прямо в корне)
            else -> {
                val cardData = characterCardJson.decodeFromJsonElement<CharacterCardData>(rootObject)
                ParsedCharacterCard(
                    specVersion = "v1",
                    data = cardData,
                    rawJson = rootObject
                )
            }
        }
    }
}
