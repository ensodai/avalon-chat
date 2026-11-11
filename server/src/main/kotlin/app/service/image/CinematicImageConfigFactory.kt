package app.service.image

import com.ensodai.shared.avalon.gifts.models.GiftActionType
import com.google.genai.types.*
import kotlin.random.Random

object CinematicImageConfigFactory {

    // 1. Настройки безопасности текстовой модели (Gemini)
    fun createTextSafetySettings(): List<SafetySetting> {
        return listOf(
            HarmCategory.Known.HARM_CATEGORY_HARASSMENT,
            HarmCategory.Known.HARM_CATEGORY_HATE_SPEECH,
            HarmCategory.Known.HARM_CATEGORY_SEXUALLY_EXPLICIT,
            HarmCategory.Known.HARM_CATEGORY_DANGEROUS_CONTENT
        ).map { category ->
            SafetySetting.builder()
                .category(category)
                .threshold(HarmBlockThreshold.Known.BLOCK_NONE)
                .build()
        }
    }

    // 2. Конфигурация генерации изображений (Imagen)
    fun createImageConfig(
        actionType: GiftActionType,
        era: String = "MODERN_OR_SCIFI",
        archetype: String = "DAILY_CASUAL"
    ): GenerateImagesConfig {
        val isPhoto = actionType == GiftActionType.CAMERA_PHOTO
        val isFantasy = era == "FANTASY_OR_HISTORICAL"
        
        val aspectRatio = if (isPhoto) "4:3" else "9:16"

        // Базовый список исключений для реализма и естественности
        val baseNegative = mutableListOf(
            "airbrushed", "plastic skin", "perfect symmetry", "idealized body", "generic instagram model",
            "heavy makeup", "perfect teeth", "artificial pose", "studio lighting", "cinematic lighting",
            "color graded", "magazine cover", "fashion shoot", "bokeh", "depth of field", "blur",
            "text", "watermark", "logo", "signature", "low quality", "bad anatomy", "deformed", "distorted",
            "wax figure", "doll-like face", "porcelain skin", "perfectly styled hair", "cgi", "perfect cosmetic skin"
        )

        // Если это современная сцена, отсекаем 3D и живопись
        if (!isFantasy) {
            baseNegative.addAll(listOf("digital art", "painting", "illustration", "anime", "cartoon", "3d render", "unreal engine"))
        } else {
            // В фэнтези допускаем художественность, но не мультяшность
            baseNegative.addAll(listOf("anime", "cartoon", "video game graphic", "plastic", "neon"))
        }

        // Тонкая настройка негатива под сценарий
        when (archetype) {
            "DRAMATIC_EMOTIONAL" -> {
                baseNegative.addAll(listOf("evil", "maniacal", "aggressive", "angry", "violent", "horror", "scary"))
            }
            "DAILY_CASUAL", "CONVERSATIONAL" -> {
                baseNegative.addAll(listOf("theatrical", "dramatic lighting", "overposed", "model-like", "staged photo"))
            }
            "RPG_ADVENTURE" -> {
                baseNegative.addAll(listOf("modern", "smartphone", "casual clothes", "plastic", "neon"))
            }
            "SCI_FI_CYBERPUNK" -> {
                baseNegative.addAll(listOf("medieval", "antique", "vintage", "rustic"))
            }
        }

        return GenerateImagesConfig.builder()
            .numberOfImages(1)
            .safetyFilterLevel(SafetyFilterLevel.Known.BLOCK_ONLY_HIGH)
            .personGeneration(PersonGeneration.Known.ALLOW_ADULT)
            .negativePrompt(baseNegative.joinToString(", "))
            .outputMimeType("image/jpeg")
            .aspectRatio(aspectRatio) 
            .guidanceScale(5.0f)
            .enhancePrompt(false) // ОТКЛЮЧАЕМ авто-улучшалку от Google
            .addWatermark(false) 
            .seed(Random.nextInt(1, Int.MAX_VALUE))
            .includeRaiReason(true)
            .build()
    }
}