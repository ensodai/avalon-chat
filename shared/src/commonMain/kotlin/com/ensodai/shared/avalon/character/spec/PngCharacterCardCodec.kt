@file:OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)

package com.ensodai.shared.avalon.character.spec

import kotlin.io.encoding.Base64

/**
 * Кроссплатформенный (KMP) кодек для извлечения и внедрения метаданных Character Card V2/V3 в PNG-файлы.
 * Не использует платформозависимые библиотеки (java.awt / javax.imageio), работает со сырыми байтами.
 */
object PngCharacterCardCodec {

    private val PNG_SIGNATURE = byteArrayOf(
        0x89.toByte(), 0x50.toByte(), 0x4E.toByte(), 0x47.toByte(),
        0x0D.toByte(), 0x0A.toByte(), 0x1A.toByte(), 0x0A.toByte()
    )

    /**
     * Извлекает JSON карточки персонажа из PNG байтов.
     * Проверяет чанк `tEXt` с ключом `ccv3` (V3), затем `chara` (V2/V1).
     *
     * @return Распарсенная карточка или null, если чанки со спецификацией не найдены.
     */
    fun extractCard(pngBytes: ByteArray): ParsedCharacterCard? {
        val jsonString = extractCardJsonString(pngBytes) ?: return null
        return runCatching {
            CharacterCardParser.parse(jsonString)
        }.getOrNull()
    }

    /**
     * Извлекает JSON-строку из PNG байтов.
     */
    fun extractCardJsonString(pngBytes: ByteArray): String? {
        if (!isPng(pngBytes)) return null

        // 1. Порядок разбора по спеке: сначала ccv3
        val rawV3 = extractTextChunk(pngBytes, "ccv3")
        if (!rawV3.isNullOrBlank()) {
            val decoded = decodeBase64ToUtf8(rawV3)
            if (decoded != null) return decoded
        }

        // 2. Если нет ccv3, ищем chara
        val rawV2 = extractTextChunk(pngBytes, "chara")
        if (!rawV2.isNullOrBlank()) {
            val decoded = decodeBase64ToUtf8(rawV2)
            if (decoded != null) return decoded
        }

        return null
    }

    /**
     * Внедряет JSON карточки персонажа в PNG-файл в виде чанка `tEXt`.
     * По умолчанию пишет в чанк с ключом `ccv3` (V3) или `chara` (V2).
     *
     * @param originalPngBytes Исходное изображение PNG.
     * @param cardJson Текст JSON карточки персонажа.
     * @param keyword Ключ чанка ("ccv3" или "chara").
     * @return Новый массив байт PNG со встроенной карточкой.
     */
    fun embedCardJson(
        originalPngBytes: ByteArray,
        cardJson: String,
        keyword: String = "ccv3"
    ): ByteArray {
        if (!isPng(originalPngBytes)) {
            throw IllegalArgumentException("Предоставленный файл не является валидным PNG.")
        }

        val base64Payload = Base64.Default.encode(cardJson.encodeToByteArray())
        val keywordBytes = keyword.encodeToByteArray()
        val textBytes = base64Payload.encodeToByteArray()

        // Структура данных tEXt: keyword + null byte (0x00) + text
        val chunkData = ByteArray(keywordBytes.size + 1 + textBytes.size)
        keywordBytes.copyInto(chunkData, 0)
        chunkData[keywordBytes.size] = 0x00.toByte()
        textBytes.copyInto(chunkData, keywordBytes.size + 1)

        val newChunk = buildPngChunk("tEXt", chunkData)

        // Ищем конец первого чанка IHDR (байт 8..сигнатура + IHDR len + 4 type + 13 data + 4 crc = 33)
        // Вставляем tEXt сразу после IHDR
        val insertOffset = findOffsetAfterIhdr(originalPngBytes)

        val result = ByteArray(originalPngBytes.size + newChunk.size)
        originalPngBytes.copyInto(result, destinationOffset = 0, startIndex = 0, endIndex = insertOffset)
        newChunk.copyInto(result, destinationOffset = insertOffset)
        originalPngBytes.copyInto(
            result,
            destinationOffset = insertOffset + newChunk.size,
            startIndex = insertOffset,
            endIndex = originalPngBytes.size
        )

        return result
    }

    /**
     * Проверяет, начинается ли файл с PNG-сигнатуры.
     */
    fun isPng(bytes: ByteArray): Boolean {
        if (bytes.size < 8) return false
        for (i in 0 until 8) {
            if (bytes[i] != PNG_SIGNATURE[i]) return false
        }
        return true
    }

    /**
     * Ищет текстовый чанк `tEXt` с заданным keyword.
     */
    private fun extractTextChunk(pngBytes: ByteArray, targetKeyword: String): String? {
        var offset = 8
        while (offset + 8 <= pngBytes.size) {
            val length = readInt32BE(pngBytes, offset)
            if (length < 0) break // Некорректный размер

            val chunkType = pngBytes.decodeToString(offset + 4, offset + 8)
            val dataOffset = offset + 8
            val nextChunkOffset = dataOffset + length + 4 // +4 на CRC

            if (chunkType == "tEXt" && dataOffset + length <= pngBytes.size) {
                // Ищем нулевой байт-разделитель между keyword и значением
                var nullByteIndex = -1
                for (i in dataOffset until (dataOffset + length)) {
                    if (pngBytes[i] == 0.toByte()) {
                        nullByteIndex = i
                        break
                    }
                }

                if (nullByteIndex != -1) {
                    val keyword = pngBytes.decodeToString(dataOffset, nullByteIndex)
                    if (keyword.equals(targetKeyword, ignoreCase = true)) {
                        return pngBytes.decodeToString(nullByteIndex + 1, dataOffset + length)
                    }
                }
            }

            if (chunkType == "IEND") break
            offset = nextChunkOffset
        }
        return null
    }

    private fun decodeBase64ToUtf8(base64Text: String): String? {
        val clean = base64Text.trim()
        return runCatching {
            Base64.Default.decode(clean).decodeToString()
        }.getOrNull()
    }

    private fun findOffsetAfterIhdr(pngBytes: ByteArray): Int {
        var offset = 8
        while (offset + 8 <= pngBytes.size) {
            val length = readInt32BE(pngBytes, offset)
            val chunkType = pngBytes.decodeToString(offset + 4, offset + 8)
            val nextChunkOffset = offset + 8 + length + 4
            if (chunkType == "IHDR") {
                return nextChunkOffset
            }
            offset = nextChunkOffset
        }
        return 33 // Стандартное смещение после IHDR (8 + 4 + 4 + 13 + 4)
    }

    private fun buildPngChunk(type: String, data: ByteArray): ByteArray {
        val typeBytes = type.encodeToByteArray()
        val length = data.size
        val chunk = ByteArray(4 + 4 + length + 4)

        // 1. Длина (4 байта BE)
        writeInt32BE(chunk, 0, length)

        // 2. Тип чанка (4 байта)
        typeBytes.copyInto(chunk, 4)

        // 3. Данные чанка
        data.copyInto(chunk, 8)

        // 4. CRC32 (по типу и данным)
        val crcData = ByteArray(4 + length)
        typeBytes.copyInto(crcData, 0)
        data.copyInto(crcData, 4)
        val crcValue = Crc32Calculator.calculate(crcData)
        writeInt32BE(chunk, 8 + length, crcValue.toInt())

        return chunk
    }

    private fun readInt32BE(bytes: ByteArray, offset: Int): Int {
        return ((bytes[offset].toInt() and 0xFF) shl 24) or
               ((bytes[offset + 1].toInt() and 0xFF) shl 16) or
               ((bytes[offset + 2].toInt() and 0xFF) shl 8) or
               (bytes[offset + 3].toInt() and 0xFF)
    }

    private fun writeInt32BE(bytes: ByteArray, offset: Int, value: Int) {
        bytes[offset] = ((value ushr 24) and 0xFF).toByte()
        bytes[offset + 1] = ((value ushr 16) and 0xFF).toByte()
        bytes[offset + 2] = ((value ushr 8) and 0xFF).toByte()
        bytes[offset + 3] = (value and 0xFF).toByte()
    }
}

/**
 * Кроссплатформенный калькулятор CRC32 по стандарту ISO 3309 / PNG.
 */
object Crc32Calculator {
    private val crcTable = LongArray(256) { i ->
        var c = i.toLong()
        for (k in 0 until 8) {
            c = if ((c and 1L) != 0L) {
                0xEDB88320L xor (c ushr 1)
            } else {
                c ushr 1
            }
        }
        c
    }

    fun calculate(data: ByteArray, offset: Int = 0, length: Int = data.size): Long {
        var crc = 0xFFFFFFFFL
        for (i in offset until (offset + length)) {
            val byteVal = data[i].toInt() and 0xFF
            val tableIndex = ((crc xor byteVal.toLong()) and 0xFFL).toInt()
            crc = crcTable[tableIndex] xor (crc ushr 8)
        }
        return (crc xor 0xFFFFFFFFL) and 0xFFFFFFFFL
    }
}
