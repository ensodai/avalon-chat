package com.ensodai.avalon.data.utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object ThumbHashDecoder {

    /**
     * Превращает строку ThumbHash (Base64) в Compose ImageBitmap.
     * Возвращает null, если строка повреждена или пустая.
     */
    @OptIn(ExperimentalEncodingApi::class)
    fun decodeString(base64Hash: String?): ImageBitmap? {
        if (base64Hash.isNullOrBlank()) return null

        return try {
            val normalizedHash = base64Hash
                .trim()
                .replace("\n", "")
                .replace("\r", "")

            // Match legacy Android behavior: URL-safe Base64 with optional padding.
            val hashBytes = Base64.UrlSafe
                .withPadding(Base64.PaddingOption.ABSENT_OPTIONAL)
                .decode(normalizedHash)

            // 2. Отдаем байты в алгоритм ThumbHash
            val thumbImage = ThumbHash.thumbHashToRGBA(hashBytes)
            val rgba = thumbImage.rgba ?: return null

            val width = thumbImage.width
            val height = thumbImage.height
            rgbaToBmp(rgba, width, height).decodeToImageBitmap()
        } catch (_: IllegalArgumentException) {
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun rgbaToBmp(rgba: ByteArray, width: Int, height: Int): ByteArray {
        val bytesPerPixel = 3
        val rowStride = width * bytesPerPixel
        val paddedRowStride = ((rowStride + 3) / 4) * 4
        val pixelDataSize = paddedRowStride * height
        val fileSize = BMP_HEADER_SIZE + DIB_HEADER_SIZE + pixelDataSize
        val result = ByteArray(fileSize)

        writeAscii(result, 0, "BM")
        writeIntLE(result, 2, fileSize)
        writeIntLE(result, 10, BMP_HEADER_SIZE + DIB_HEADER_SIZE)

        writeIntLE(result, 14, DIB_HEADER_SIZE)
        writeIntLE(result, 18, width)
        writeIntLE(result, 22, height)
        writeShortLE(result, 26, 1)
        writeShortLE(result, 28, 24)
        writeIntLE(result, 34, pixelDataSize)

        var dst = BMP_HEADER_SIZE + DIB_HEADER_SIZE
        for (y in height - 1 downTo 0) {
            val rowStart = y * width * 4
            for (x in 0 until width) {
                val src = rowStart + x * 4
                val r = rgba[src].toInt() and 0xFF
                val g = rgba[src + 1].toInt() and 0xFF
                val b = rgba[src + 2].toInt() and 0xFF
                val a = rgba[src + 3].toInt() and 0xFF
                val alpha = a / 255f

                result[dst++] = (b * alpha).toInt().toByte()
                result[dst++] = (g * alpha).toInt().toByte()
                result[dst++] = (r * alpha).toInt().toByte()
            }
            dst += paddedRowStride - rowStride
        }

        return result
    }

    private fun writeAscii(buffer: ByteArray, offset: Int, value: String) {
        value.forEachIndexed { index, char ->
            buffer[offset + index] = char.code.toByte()
        }
    }

    private fun writeShortLE(buffer: ByteArray, offset: Int, value: Int) {
        buffer[offset] = (value and 0xFF).toByte()
        buffer[offset + 1] = ((value shr 8) and 0xFF).toByte()
    }

    private fun writeIntLE(buffer: ByteArray, offset: Int, value: Int) {
        buffer[offset] = (value and 0xFF).toByte()
        buffer[offset + 1] = ((value shr 8) and 0xFF).toByte()
        buffer[offset + 2] = ((value shr 16) and 0xFF).toByte()
        buffer[offset + 3] = ((value shr 24) and 0xFF).toByte()
    }

    private const val BMP_HEADER_SIZE = 14
    private const val DIB_HEADER_SIZE = 40
}
