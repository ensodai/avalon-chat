package com.ensodai.shared.avalon.character.spec

import com.ensodai.shared.avalon.character.models.CharacterLorebookEntryInput
import com.ensodai.shared.avalon.character.models.CreateStandaloneLorebookRequest
import com.ensodai.shared.avalon.character.models.LorebookEntryPosition
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.uuid.Uuid

/**
 * 📚 Универсальный парсер для автономных файлов баз знаний (World Info / Lorebook).
 * Поддерживает форматы:
 * 1. SillyTavern (modern V2/V3): { "name": "...", "entries": [ {...} ] }
 * 2. SillyTavern / TavernAI (legacy): { "entries": { "0": {...}, "1": {...} } }
 * 3. Обертки карточки персонажа: { "character_book": { ... } } или { "data": { ... } }
 * 4. Agnai / Chub экспорты World Info.
 */
object StandaloneLorebookParser {

    fun parse(
        jsonString: String,
        fallbackName: String = "Импортированная база знаний",
        isPublic: Boolean = false,
        attachToCharacterId: Uuid? = null
    ): CreateStandaloneLorebookRequest {
        val rootElement = characterCardJson.parseToJsonElement(jsonString)
        val rootObj = when (rootElement) {
            is JsonObject -> {
                rootElement["character_book"]?.jsonObject
                    ?: rootElement["data"]?.jsonObject
                    ?: rootElement
            }
            else -> throw IllegalArgumentException("Неверный формат JSON: корневой элемент должен быть объектом")
        }

        val name = rootObj["name"]?.jsonPrimitive?.contentOrNull?.trim()?.ifBlank { null }
            ?: fallbackName
        val description = rootObj["description"]?.jsonPrimitive?.contentOrNull?.trim()?.ifBlank { null }
        val scanDepth = rootObj["scan_depth"]?.jsonPrimitive?.intOrNull ?: 5
        val tokenBudget = rootObj["token_budget"]?.jsonPrimitive?.intOrNull
        val recursiveScanning = rootObj["recursive_scanning"]?.jsonPrimitive?.booleanOrNull ?: false

        val entriesElement = rootObj["entries"]
        val parsedEntries = mutableListOf<CharacterLorebookEntryInput>()

        when (entriesElement) {
            is JsonArray -> {
                for ((index, element) in entriesElement.withIndex()) {
                    if (element is JsonObject) {
                        parseEntry(element, index)?.let { parsedEntries.add(it) }
                    }
                }
            }
            is JsonObject -> {
                var index = 0
                for ((_, element) in entriesElement) {
                    if (element is JsonObject) {
                        parseEntry(element, index++)?.let { parsedEntries.add(it) }
                    }
                }
            }
            else -> {
                // Если entries отсутствуют или пусты
            }
        }

        return CreateStandaloneLorebookRequest(
            name = name,
            description = description,
            scanDepth = scanDepth,
            tokenBudget = tokenBudget,
            recursiveScanning = recursiveScanning,
            isPublic = isPublic,
            attachToCharacterId = attachToCharacterId,
            entries = parsedEntries
        )
    }

    private fun parseEntry(obj: JsonObject, defaultIndex: Int): CharacterLorebookEntryInput? {
        val content = obj["content"]?.jsonPrimitive?.contentOrNull?.trim() ?: ""
        if (content.isBlank()) {
            return null
        }

        val keysList = extractStringList(obj["keys"] ?: obj["key"])
        val secondaryKeysList = extractStringList(obj["secondary_keys"] ?: obj["keysecondary"])

        val comment = obj["comment"]?.jsonPrimitive?.contentOrNull
            ?: obj["name"]?.jsonPrimitive?.contentOrNull
            ?: obj["title"]?.jsonPrimitive?.contentOrNull

        val enabled = obj["enabled"]?.jsonPrimitive?.booleanOrNull
            ?: obj["disable"]?.jsonPrimitive?.booleanOrNull?.not()
            ?: true

        val constant = obj["constant"]?.jsonPrimitive?.booleanOrNull ?: false
        val selective = obj["selective"]?.jsonPrimitive?.booleanOrNull ?: false
        val order = obj["insertion_order"]?.jsonPrimitive?.intOrNull
            ?: obj["order"]?.jsonPrimitive?.intOrNull
            ?: defaultIndex

        val positionStr = obj["position"]?.jsonPrimitive?.contentOrNull?.lowercase()
        val positionInt = obj["position"]?.jsonPrimitive?.intOrNull
        val position = when {
            positionStr != null -> when (positionStr) {
                "after_char" -> LorebookEntryPosition.AFTER_CHAR
                "before_desc" -> LorebookEntryPosition.BEFORE_DESC
                "after_desc" -> LorebookEntryPosition.AFTER_DESC
                "personality" -> LorebookEntryPosition.PERSONALITY
                "scenario" -> LorebookEntryPosition.SCENARIO
                "at_depth", "depth" -> LorebookEntryPosition.AT_DEPTH
                else -> LorebookEntryPosition.BEFORE_CHAR
            }
            positionInt != null -> when (positionInt) {
                1 -> LorebookEntryPosition.AFTER_CHAR
                else -> LorebookEntryPosition.BEFORE_CHAR
            }
            else -> LorebookEntryPosition.BEFORE_CHAR
        }

        val depth = obj["depth"]?.jsonPrimitive?.intOrNull ?: 4
        val useRegex = obj["use_regex"]?.jsonPrimitive?.booleanOrNull
            ?: obj["useRegex"]?.jsonPrimitive?.booleanOrNull
            ?: false

        return CharacterLorebookEntryInput(
            keys = keysList,
            secondaryKeys = secondaryKeysList.takeIf { it.isNotEmpty() },
            content = content,
            comment = comment?.trim()?.ifBlank { null },
            enabled = enabled,
            constant = constant,
            selective = selective,
            insertionOrder = order,
            position = position,
            depth = depth,
            useRegex = useRegex
        )
    }

    private fun extractStringList(element: JsonElement?): List<String> {
        if (element == null) return emptyList()
        return when (element) {
            is JsonArray -> element.mapNotNull { it.jsonPrimitive.contentOrNull?.trim() }.filter { it.isNotBlank() }
            is JsonPrimitive -> {
                element.contentOrNull
                    ?.split(",", "\n")
                    ?.map { it.trim() }
                    ?.filter { it.isNotBlank() }
                    ?: emptyList()
            }
            else -> emptyList()
        }
    }
}
