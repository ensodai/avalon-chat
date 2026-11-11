package app.service.image

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.util.*
import javax.imageio.ImageIO
import kotlin.math.max

object ImagePreviewGenerator {

    /**
     * Принимает сырые байты картинки (от нейросети или клиента),
     * сжимает её и возвращает короткую Base64-строку ThumbHash.
     */
    fun generateThumbHashString(imageBytes: ByteArray): String {
        // 1. Читаем картинку
        val image: BufferedImage = ImageIO.read(ByteArrayInputStream(imageBytes))
            ?: throw IllegalArgumentException("Не удалось прочитать байты изображения")

        return generateThumbHashString(image)
    }

    /**
     * Перегруженный метод, если BufferedImage уже есть в памяти.
     */
    fun generateThumbHashString(originalImage: BufferedImage): String {
        // 2. ThumbHash требует картинку не больше 100x100.
        // Делаем ресайз с сохранением пропорций.
        val maxDim = 100
        val ratio = originalImage.width.toDouble() / originalImage.height.toDouble()
        val thumbWidth = max(1, if (ratio > 1) maxDim else (maxDim * ratio).toInt())
        val thumbHeight = max(1, if (ratio > 1) (maxDim / ratio).toInt() else maxDim)

        // Используем TYPE_INT_ARGB, чтобы сохранить прозрачность (важно для стикеров!)
        val thumbnail = BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_ARGB)
        val graphics = thumbnail.createGraphics()
        graphics.drawImage(originalImage, 0, 0, thumbWidth, thumbHeight, null)
        graphics.dispose()

        // 3. Достаем пиксели из сжатой копии
        val pixels = IntArray(thumbWidth * thumbHeight)
        thumbnail.getRGB(0, 0, thumbWidth, thumbHeight, pixels, 0, thumbWidth)

        // 4. ThumbHash ждет массив байтов, где идут подряд R, G, B, A
        val rgba = ByteArray(thumbWidth * thumbHeight * 4)
        for (i in pixels.indices) {
            val pixel = pixels[i]
            rgba[i * 4 + 0] = ((pixel shr 16) and 0xFF).toByte() // R
            rgba[i * 4 + 1] = ((pixel shr 8) and 0xFF).toByte()  // G
            rgba[i * 4 + 2] = (pixel and 0xFF).toByte()          // B
            rgba[i * 4 + 3] = ((pixel shr 24) and 0xFF).toByte() // A
        }

        // 5. Генерируем сам хэш
        val thumbHashBytes = ThumbHash.rgbaToThumbHash(thumbWidth, thumbHeight, rgba)

        // 6. Кодируем массив байтов в строку (Base64 URL-safe без паддинга - самый компактный вариант)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(thumbHashBytes)
    }
}