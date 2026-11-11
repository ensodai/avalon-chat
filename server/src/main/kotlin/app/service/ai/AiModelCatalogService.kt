package app.service.ai

import com.ensodai.shared.avalon.user.models.AiModelCategory
import com.ensodai.shared.avalon.user.models.AiModelCatalogItem
import com.ensodai.shared.avalon.user.models.AiProviderType
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import org.koin.core.annotation.Single
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration.Companion.hours

@Single
class AiModelCatalogService(
    private val httpClient: HttpClient,
    private val coroutineScope: CoroutineScope
) {
    private val logger = LoggerFactory.getLogger(AiModelCatalogService::class.java)

    // Кэш моделей по ключу "PROVIDER:CATEGORY"
    private val catalogCache = ConcurrentHashMap<String, List<AiModelCatalogItem>>()

    init {
        // 1. Инициализируем статическими кураторскими каталогами
        initCuratedDefaults()

        // 2. Фоновый Worker для обновления каталога OpenRouter раз в 1 час
        coroutineScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    refreshOpenRouterModels()
                } catch (e: Exception) {
                    logger.error("Failed to refresh OpenRouter models catalog: ${e.message}")
                }
                delay(1.hours)
            }
        }
    }

    fun getModels(provider: AiProviderType, category: AiModelCategory): List<AiModelCatalogItem> {
        val key = "${provider.name}:${category.name}"
        return catalogCache[key] ?: getDefaultModels(provider, category)
    }

    private suspend fun refreshOpenRouterModels() {
        logger.info("Refreshing OpenRouter models catalog from https://openrouter.ai/api/v1/models ...")
        val response = httpClient.get("https://openrouter.ai/api/v1/models").body<OpenRouterModelsResponse>()

        val rawModels = response.data ?: return
        logger.info("Fetched ${rawModels.size} models from OpenRouter")

        val textModels = mutableListOf<AiModelCatalogItem>()
        val imageModels = mutableListOf<AiModelCatalogItem>()
        val ttsModels = mutableListOf<AiModelCatalogItem>()
        val summaryModels = mutableListOf<AiModelCatalogItem>()
        val promptModels = mutableListOf<AiModelCatalogItem>()

        // Рекомендуемые топовые модели OpenRouter
        val topTextIds = listOf("google/gemini-2.5-flash", "anthropic/claude-3.7-sonnet", "openai/gpt-4o", "deepseek/deepseek-r1", "meta-llama/llama-3.3-70b-instruct")
        val topImageIds = listOf("google/gemini-2.5-flash-image", "google/gemini-3.1-flash-image", "openai/gpt-5-image", "openai/gpt-5-image-mini")
        val topTtsIds = listOf("openai/gpt-audio", "openai/gpt-audio-mini", "google/lyria-3-pro-preview")
        val topSummaryIds = listOf("google/gemini-2.5-flash-lite", "openai/gpt-4o-mini", "meta-llama/llama-3.3-70b-instruct")

        for (m in rawModels) {
            val id = m.id ?: continue
            if (id.startsWith("openrouter/auto")) continue

            val name = m.name ?: id
            val desc = m.description
            val ctx = m.context_length?.let { formatContext(it) }
            val outputMods = m.architecture?.output_modalities.orEmpty()
            val modality = m.architecture?.modality?.lowercase().orEmpty()

            val isOutputImage = outputMods.contains("image") || modality.contains("->text+image") || modality.endsWith("->image")
            val isOutputAudio = outputMods.contains("audio") || modality.contains("->text+audio") || modality.endsWith("->audio")
            val isOutputText = outputMods.contains("text") || modality.contains("->text") || (outputMods.isEmpty() && modality.isEmpty())

            val promptPrice = m.pricing?.prompt?.toDoubleOrNull()?.let { formatTokenPrice(it) }
            val compPrice = m.pricing?.completion?.toDoubleOrNull()?.let { formatTokenPrice(it) }
            val imagePrice = m.pricing?.image_output?.toDoubleOrNull()?.let { formatImagePrice(it) }
                ?: m.pricing?.image?.toDoubleOrNull()?.let { formatTokenPrice(it) }

            if (isOutputImage) {
                val isRec = topImageIds.contains(id)
                val badge = when (id) {
                    "google/gemini-2.5-flash-image" -> "🔥 Топ выбор"
                    "google/gemini-3.1-flash-image" -> "🎨 Высокое качество"
                    "openai/gpt-5-image" -> "💎 Премиум"
                    else -> null
                }
                imageModels.add(
                    AiModelCatalogItem(
                        id = id,
                        name = name,
                        provider = AiProviderType.OPENROUTER,
                        category = AiModelCategory.IMAGE,
                        contextWindow = ctx,
                        promptPrice = imagePrice ?: promptPrice,
                        completionPrice = compPrice,
                        description = desc,
                        isRecommended = isRec,
                        badge = badge
                    )
                )
            }

            if (isOutputAudio) {
                val isRec = topTtsIds.contains(id)
                val badge = if (id == "openai/gpt-audio") "🔥 Топ выбор" else null
                ttsModels.add(
                    AiModelCatalogItem(
                        id = id,
                        name = name,
                        provider = AiProviderType.OPENROUTER,
                        category = AiModelCategory.TTS,
                        contextWindow = ctx,
                        promptPrice = promptPrice,
                        completionPrice = compPrice,
                        description = desc,
                        isRecommended = isRec,
                        badge = badge
                    )
                )
            }

            if (isOutputText) {
                val isRec = topTextIds.contains(id)
                val badge = when (id) {
                    "google/gemini-2.5-flash" -> "🔥 Топ выбор"
                    "anthropic/claude-3.7-sonnet" -> "💎 Шедевр"
                    "openai/gpt-4o" -> "🧠 Интеллект"
                    "deepseek/deepseek-r1" -> "💡 Мыслитель"
                    else -> null
                }
                val item = AiModelCatalogItem(
                    id = id,
                    name = name,
                    provider = AiProviderType.OPENROUTER,
                    category = AiModelCategory.TEXT,
                    contextWindow = ctx,
                    promptPrice = promptPrice,
                    completionPrice = compPrice,
                    description = desc,
                    isRecommended = isRec,
                    badge = badge
                )
                textModels.add(item)
                promptModels.add(item.copy(category = AiModelCategory.IMAGE_PROMPT))

                val sumRec = topSummaryIds.contains(id)
                val sumBadge = if (id == "google/gemini-2.5-flash-lite") "🔥 Топ выбор" else null
                summaryModels.add(item.copy(category = AiModelCategory.SUMMARY, isRecommended = sumRec, badge = sumBadge))
            }
        }

        // Сортируем: сначала рекомендуемые (в заданном порядке), затем по имени
        if (textModels.isNotEmpty()) {
            catalogCache["${AiProviderType.OPENROUTER.name}:${AiModelCategory.TEXT.name}"] = sortModels(textModels, topTextIds)
            catalogCache["${AiProviderType.OPENROUTER.name}:${AiModelCategory.IMAGE_PROMPT.name}"] = sortModels(promptModels, topTextIds)
            catalogCache["${AiProviderType.OPENROUTER.name}:${AiModelCategory.SUMMARY.name}"] = sortModels(summaryModels, topSummaryIds)
        }
        if (imageModels.isNotEmpty()) {
            catalogCache["${AiProviderType.OPENROUTER.name}:${AiModelCategory.IMAGE.name}"] = sortModels(imageModels, topImageIds)
        }
        if (ttsModels.isNotEmpty()) {
            catalogCache["${AiProviderType.OPENROUTER.name}:${AiModelCategory.TTS.name}"] = sortModels(ttsModels, topTtsIds)
        }
    }

    private fun sortModels(list: List<AiModelCatalogItem>, priorityIds: List<String>): List<AiModelCatalogItem> {
        val (recommended, others) = list.partition { it.isRecommended }
        val sortedRecommended = recommended.sortedBy { priorityIds.indexOf(it.id).let { idx -> if (idx >= 0) idx else 999 } }
        val sortedOthers = others.sortedBy { it.name }
        return sortedRecommended + sortedOthers
    }

    private fun formatContext(tokens: Long): String {
        return when {
            tokens >= 1_000_000 -> "${tokens / 1_000_000}M"
            tokens >= 1_000 -> "${tokens / 1_000}k"
            else -> "$tokens"
        }
    }

    private fun formatTokenPrice(perToken: Double): String {
        val perMillion = perToken * 1_000_000
        return when {
            perMillion == 0.0 -> "Бесплатно"
            perMillion < 0.01 -> "$${String.format("%.4f", perMillion)} / 1M"
            perMillion < 1.0 -> "$${String.format("%.3f", perMillion)} / 1M"
            else -> "$${String.format("%.2f", perMillion)} / 1M"
        }
    }

    private fun formatImagePrice(price: Double): String {
        val perImage = price * 1000
        return "$${String.format("%.3f", perImage)} / фото"
    }

    private fun initCuratedDefaults() {
        // --- GOOGLE AI STUDIO ---
        val googleText = listOf(
            AiModelCatalogItem(
                id = "gemini-3.1-flash-lite",
                name = "Gemini 3.1 Flash Lite",
                provider = AiProviderType.GOOGLE,
                category = AiModelCategory.TEXT,
                contextWindow = "1M токенов",
                promptPrice = "$0.038 / 1M",
                completionPrice = "$0.15 / 1M",
                description = "Ультра-быстрая и лёгкая модель для мгновенных ответов",
                isRecommended = true,
                badge = "🔥 Топ выбор"
            ),
            AiModelCatalogItem(
                id = "gemini-2.5-flash",
                name = "Gemini 2.5 Flash",
                provider = AiProviderType.GOOGLE,
                category = AiModelCategory.TEXT,
                contextWindow = "1M токенов",
                promptPrice = "$0.075 / 1M",
                completionPrice = "$0.30 / 1M",
                description = "Флагманский баланс скорости и ума нового поколения",
                isRecommended = true,
                badge = "⚡ Сбалансированная"
            ),
            AiModelCatalogItem(
                id = "gemini-2.5-pro",
                name = "Gemini 2.5 Pro",
                provider = AiProviderType.GOOGLE,
                category = AiModelCategory.TEXT,
                contextWindow = "2M токенов",
                promptPrice = "$1.25 / 1M",
                completionPrice = "$5.00 / 1M",
                description = "Максимальная глубина рассуждений и отыгрыша персонажей",
                isRecommended = true,
                badge = "💎 Глубокий разум"
            ),
            AiModelCatalogItem(
                id = "gemini-3.5-flash",
                name = "Gemini 3.5 Flash",
                provider = AiProviderType.GOOGLE,
                category = AiModelCategory.TEXT,
                contextWindow = "1M токенов",
                promptPrice = "$0.075 / 1M",
                completionPrice = "$0.30 / 1M",
                description = "Продвинутая модель высокой производительности",
                isRecommended = false
            ),
            AiModelCatalogItem(
                id = "gemini-3.7-flash",
                name = "Gemini 3.7 Flash",
                provider = AiProviderType.GOOGLE,
                category = AiModelCategory.TEXT,
                contextWindow = "1M токенов",
                promptPrice = "$0.075 / 1M",
                completionPrice = "$0.30 / 1M",
                description = "Экспериментальная модель с продвинутым мышлением",
                isRecommended = false
            )
        )

        val googleTts = listOf(
            AiModelCatalogItem(
                id = "gemini-2.5-flash-preview-tts",
                name = "Gemini 2.5 Flash TTS",
                provider = AiProviderType.GOOGLE,
                category = AiModelCategory.TTS,
                contextWindow = null,
                promptPrice = "$0.075 / 1M токенов",
                completionPrice = null,
                description = "Нативный синтез выразительной речи персонажей",
                isRecommended = true,
                badge = "🔥 Топ выбор"
            ),
            AiModelCatalogItem(
                id = "gemini-3.1-flash-tts-preview",
                name = "Gemini 3.1 Flash TTS",
                provider = AiProviderType.GOOGLE,
                category = AiModelCategory.TTS,
                contextWindow = null,
                promptPrice = "$0.075 / 1M токенов",
                completionPrice = null,
                description = "Новое поколение синтеза речи Gemini",
                isRecommended = true,
                badge = "✨ Новое поколение"
            ),
            AiModelCatalogItem(
                id = "gemini-2.5-pro-preview-tts",
                name = "Gemini 2.5 Pro TTS",
                provider = AiProviderType.GOOGLE,
                category = AiModelCategory.TTS,
                contextWindow = null,
                promptPrice = "$1.25 / 1M токенов",
                completionPrice = null,
                description = "Продвинутая модель эмоциональной озвучки",
                isRecommended = true,
                badge = "🎙 Премиум"
            )
        )

        val googleImage = listOf(
            AiModelCatalogItem(
                id = "gemini-2.5-flash-image",
                name = "Gemini 2.5 Flash Image",
                provider = AiProviderType.GOOGLE,
                category = AiModelCategory.IMAGE,
                contextWindow = null,
                promptPrice = "$0.030 / фото",
                completionPrice = null,
                description = "Быстрая генерация аниме и реалистичных артов",
                isRecommended = true,
                badge = "🔥 Топ выбор"
            ),
            AiModelCatalogItem(
                id = "gemini-3.1-flash-image",
                name = "Gemini 3.1 Flash Image",
                provider = AiProviderType.GOOGLE,
                category = AiModelCategory.IMAGE,
                contextWindow = null,
                promptPrice = "$0.030 / фото",
                completionPrice = null,
                description = "Высокая детализация и чёткость генерации",
                isRecommended = true,
                badge = "🎨 Высокое качество"
            ),
            AiModelCatalogItem(
                id = "gemini-3.1-flash-lite-image",
                name = "Gemini 3.1 Flash Lite Image",
                provider = AiProviderType.GOOGLE,
                category = AiModelCategory.IMAGE,
                contextWindow = null,
                promptPrice = "$0.015 / фото",
                completionPrice = null,
                description = "Лёгкая и мгновенная генерация артов",
                isRecommended = true,
                badge = "⚡ Молния"
            )
        )

        val googleSummary = googleText.map { item ->
            val isRec = item.id in setOf("gemini-3.1-flash-lite", "gemini-2.5-flash")
            val badge = when (item.id) {
                "gemini-3.1-flash-lite" -> "🔥 Идеально"
                "gemini-2.5-flash" -> "🧠 Точная"
                else -> null
            }
            item.copy(
                category = AiModelCategory.SUMMARY,
                isRecommended = isRec,
                badge = badge
            )
        }

        catalogCache["${AiProviderType.GOOGLE.name}:${AiModelCategory.TEXT.name}"] = googleText
        catalogCache["${AiProviderType.GOOGLE.name}:${AiModelCategory.TTS.name}"] = googleTts
        catalogCache["${AiProviderType.GOOGLE.name}:${AiModelCategory.IMAGE.name}"] = googleImage
        catalogCache["${AiProviderType.GOOGLE.name}:${AiModelCategory.IMAGE_PROMPT.name}"] = googleText.map { it.copy(category = AiModelCategory.IMAGE_PROMPT) }
        catalogCache["${AiProviderType.GOOGLE.name}:${AiModelCategory.SUMMARY.name}"] = googleSummary

        // --- OPENAI DIRECT ---
        val openAiText = listOf(
            AiModelCatalogItem(
                id = "gpt-4o",
                name = "GPT-4o",
                provider = AiProviderType.OPENAI,
                category = AiModelCategory.TEXT,
                contextWindow = "128k токенов",
                promptPrice = "$2.50 / 1M",
                completionPrice = "$10.00 / 1M",
                description = "Флагманская мультимодальная модель OpenAI",
                isRecommended = true,
                badge = "🔥 Топ выбор"
            ),
            AiModelCatalogItem(
                id = "gpt-4o-mini",
                name = "GPT-4o Mini",
                provider = AiProviderType.OPENAI,
                category = AiModelCategory.TEXT,
                contextWindow = "128k токенов",
                promptPrice = "$0.15 / 1M",
                completionPrice = "$0.60 / 1M",
                description = "Быстрая и супер-экономная модель для чата",
                isRecommended = true,
                badge = "⚡ Эконом"
            ),
            AiModelCatalogItem(
                id = "o3-mini",
                name = "o3-mini",
                provider = AiProviderType.OPENAI,
                category = AiModelCategory.TEXT,
                contextWindow = "200k токенов",
                promptPrice = "$1.10 / 1M",
                completionPrice = "$4.40 / 1M",
                description = "Продвинутая модель логических рассуждений",
                isRecommended = true,
                badge = "💎 Мыслитель"
            )
        )

        val openAiTts = listOf(
            AiModelCatalogItem(
                id = "tts-1",
                name = "TTS-1",
                provider = AiProviderType.OPENAI,
                category = AiModelCategory.TTS,
                contextWindow = null,
                promptPrice = "$15.00 / 1M симв.",
                completionPrice = null,
                description = "Стандартная быстрая озвучка от OpenAI",
                isRecommended = true,
                badge = "🔥 Топ выбор"
            ),
            AiModelCatalogItem(
                id = "tts-1-hd",
                name = "TTS-1 HD",
                provider = AiProviderType.OPENAI,
                category = AiModelCategory.TTS,
                contextWindow = null,
                promptPrice = "$30.00 / 1M симв.",
                completionPrice = null,
                description = "Высококачественная чистая озвучка",
                isRecommended = true,
                badge = "💎 HD Качество"
            )
        )

        val openAiImage = listOf(
            AiModelCatalogItem(
                id = "dall-e-3",
                name = "DALL-E 3",
                provider = AiProviderType.OPENAI,
                category = AiModelCategory.IMAGE,
                contextWindow = null,
                promptPrice = "$0.040 / фото",
                completionPrice = null,
                description = "Флагманский генератор художественных сцен",
                isRecommended = true,
                badge = "🔥 Топ выбор"
            ),
            AiModelCatalogItem(
                id = "dall-e-2",
                name = "DALL-E 2",
                provider = AiProviderType.OPENAI,
                category = AiModelCategory.IMAGE,
                contextWindow = null,
                promptPrice = "$0.020 / фото",
                completionPrice = null,
                description = "Экономный генератор картинок",
                isRecommended = false
            )
        )

        val openAiSummary = openAiText.map { item ->
            val isRec = item.id == "gpt-4o-mini"
            val badge = if (isRec) "🔥 Топ выбор" else null
            item.copy(
                category = AiModelCategory.SUMMARY,
                isRecommended = isRec,
                badge = badge
            )
        }

        catalogCache["${AiProviderType.OPENAI.name}:${AiModelCategory.TEXT.name}"] = openAiText
        catalogCache["${AiProviderType.OPENAI.name}:${AiModelCategory.TTS.name}"] = openAiTts
        catalogCache["${AiProviderType.OPENAI.name}:${AiModelCategory.IMAGE.name}"] = openAiImage
        catalogCache["${AiProviderType.OPENAI.name}:${AiModelCategory.IMAGE_PROMPT.name}"] = openAiText.map { it.copy(category = AiModelCategory.IMAGE_PROMPT) }
        catalogCache["${AiProviderType.OPENAI.name}:${AiModelCategory.SUMMARY.name}"] = openAiSummary

        // --- DEEPSEEK ---
        val deepseekText = listOf(
            AiModelCatalogItem(
                id = "deepseek-chat",
                name = "DeepSeek V3",
                provider = AiProviderType.DEEPSEEK,
                category = AiModelCategory.TEXT,
                contextWindow = "64k токенов",
                promptPrice = "$0.14 / 1M",
                completionPrice = "$0.28 / 1M",
                description = "Флагманская модель диалогов с огромным интеллектом",
                isRecommended = true,
                badge = "🔥 Топ выбор"
            ),
            AiModelCatalogItem(
                id = "deepseek-reasoner",
                name = "DeepSeek R1",
                provider = AiProviderType.DEEPSEEK,
                category = AiModelCategory.TEXT,
                contextWindow = "64k токенов",
                promptPrice = "$0.55 / 1M",
                completionPrice = "$2.19 / 1M",
                description = "Глубокие рассуждения и логика цепочек мысли",
                isRecommended = true,
                badge = "💎 Мыслитель"
            )
        )
        catalogCache["${AiProviderType.DEEPSEEK.name}:${AiModelCategory.TEXT.name}"] = deepseekText
        catalogCache["${AiProviderType.DEEPSEEK.name}:${AiModelCategory.SUMMARY.name}"] = deepseekText.map { it.copy(category = AiModelCategory.SUMMARY) }

        // --- GROQ ---
        val groqText = listOf(
            AiModelCatalogItem(
                id = "llama-3.3-70b-versatile",
                name = "Llama 3.3 70B",
                provider = AiProviderType.GROQ,
                category = AiModelCategory.TEXT,
                contextWindow = "128k токенов",
                promptPrice = "$0.59 / 1M",
                completionPrice = "$0.79 / 1M",
                description = "Молниеносный инференс на процессорах Groq LPU",
                isRecommended = true,
                badge = "🔥 Топ выбор"
            ),
            AiModelCatalogItem(
                id = "mixtral-8x7b-32768",
                name = "Mixtral 8x7B",
                provider = AiProviderType.GROQ,
                category = AiModelCategory.TEXT,
                contextWindow = "32k токенов",
                promptPrice = "$0.24 / 1M",
                completionPrice = "$0.24 / 1M",
                description = "Сверхбыстрая модель архитектуры MoE",
                isRecommended = true,
                badge = "⚡ Быстрая"
            ),
            AiModelCatalogItem(
                id = "llama-3.1-8b-instant",
                name = "Llama 3.1 8B Instant",
                provider = AiProviderType.GROQ,
                category = AiModelCategory.TEXT,
                contextWindow = "128k токенов",
                promptPrice = "$0.05 / 1M",
                completionPrice = "$0.08 / 1M",
                description = "Мгновенная генерация с низкой задержкой",
                isRecommended = true,
                badge = "⚡ Эконом"
            )
        )
        catalogCache["${AiProviderType.GROQ.name}:${AiModelCategory.TEXT.name}"] = groqText
        catalogCache["${AiProviderType.GROQ.name}:${AiModelCategory.SUMMARY.name}"] = groqText.map { it.copy(category = AiModelCategory.SUMMARY) }

        // --- OPENROUTER DEFAULT CURATED ---
        val openRouterText = listOf(
            AiModelCatalogItem(
                id = "google/gemini-2.5-flash",
                name = "Google: Gemini 2.5 Flash",
                provider = AiProviderType.OPENROUTER,
                category = AiModelCategory.TEXT,
                contextWindow = "1M токенов",
                promptPrice = "$0.075 / 1M",
                completionPrice = "$0.30 / 1M",
                description = "Флагманский баланс ума и скорости",
                isRecommended = true,
                badge = "🔥 Топ выбор"
            ),
            AiModelCatalogItem(
                id = "anthropic/claude-3.7-sonnet",
                name = "Anthropic: Claude 3.7 Sonnet",
                provider = AiProviderType.OPENROUTER,
                category = AiModelCategory.TEXT,
                contextWindow = "200k токенов",
                promptPrice = "$3.00 / 1M",
                completionPrice = "$15.00 / 1M",
                description = "Шедевр литературного ролеплея и рассуждений",
                isRecommended = true,
                badge = "💎 Шедевр"
            ),
            AiModelCatalogItem(
                id = "openai/gpt-4o",
                name = "OpenAI: GPT-4o",
                provider = AiProviderType.OPENROUTER,
                category = AiModelCategory.TEXT,
                contextWindow = "128k токенов",
                promptPrice = "$2.50 / 1M",
                completionPrice = "$10.00 / 1M",
                description = "Флагманский интеллект от OpenAI",
                isRecommended = true,
                badge = "🧠 Интеллект"
            ),
            AiModelCatalogItem(
                id = "deepseek/deepseek-r1",
                name = "DeepSeek: DeepSeek R1",
                provider = AiProviderType.OPENROUTER,
                category = AiModelCategory.TEXT,
                contextWindow = "64k токенов",
                promptPrice = "$0.55 / 1M",
                completionPrice = "$2.19 / 1M",
                description = "Логические рассуждения и глубокий анализ",
                isRecommended = true,
                badge = "💡 Мыслитель"
            ),
            AiModelCatalogItem(
                id = "meta-llama/llama-3.3-70b-instruct",
                name = "Meta: Llama 3.3 70B",
                provider = AiProviderType.OPENROUTER,
                category = AiModelCategory.TEXT,
                contextWindow = "128k токенов",
                promptPrice = "$0.12 / 1M",
                completionPrice = "$0.30 / 1M",
                description = "Мощная открытая модель от Meta",
                isRecommended = true,
                badge = "⚡ Быстрая"
            ),
            AiModelCatalogItem(
                id = "google/gemini-2.5-flash-lite",
                name = "Google: Gemini 2.5 Flash Lite",
                provider = AiProviderType.OPENROUTER,
                category = AiModelCategory.TEXT,
                contextWindow = "1M токенов",
                promptPrice = "$0.038 / 1M",
                completionPrice = "$0.15 / 1M",
                description = "Супер-экономная модель для быстрых диалогов",
                isRecommended = true,
                badge = "⚡ Эконом"
            )
        )

        val openRouterImage = listOf(
            AiModelCatalogItem(
                id = "google/gemini-2.5-flash-image",
                name = "Google: Gemini 2.5 Flash Image",
                provider = AiProviderType.OPENROUTER,
                category = AiModelCategory.IMAGE,
                contextWindow = null,
                promptPrice = "$0.030 / фото",
                completionPrice = null,
                description = "Генерация красивых артов от Google",
                isRecommended = true,
                badge = "🔥 Топ выбор"
            ),
            AiModelCatalogItem(
                id = "google/gemini-3.1-flash-image",
                name = "Google: Gemini 3.1 Flash Image",
                provider = AiProviderType.OPENROUTER,
                category = AiModelCategory.IMAGE,
                contextWindow = null,
                promptPrice = "$0.060 / фото",
                completionPrice = null,
                description = "Высокая детализация генерации артов",
                isRecommended = true,
                badge = "🎨 Высокое качество"
            ),
            AiModelCatalogItem(
                id = "openai/gpt-5-image",
                name = "OpenAI: GPT-5 Image",
                provider = AiProviderType.OPENROUTER,
                category = AiModelCategory.IMAGE,
                contextWindow = null,
                promptPrice = "$0.040 / фото",
                completionPrice = null,
                description = "Генерация художественных сцен OpenAI",
                isRecommended = true,
                badge = "💎 Премиум"
            ),
            AiModelCatalogItem(
                id = "openai/gpt-5-image-mini",
                name = "OpenAI: GPT-5 Image Mini",
                provider = AiProviderType.OPENROUTER,
                category = AiModelCategory.IMAGE,
                contextWindow = null,
                promptPrice = "$0.008 / фото",
                completionPrice = null,
                description = "Быстрая лёгкая генерация картинок",
                isRecommended = true,
                badge = "⚡ Быстрая"
            )
        )

        val openRouterTts = listOf(
            AiModelCatalogItem(
                id = "openai/gpt-audio",
                name = "OpenAI: GPT Audio",
                provider = AiProviderType.OPENROUTER,
                category = AiModelCategory.TTS,
                contextWindow = null,
                promptPrice = "$0.064 / 1M токенов",
                completionPrice = null,
                description = "Выразительная генерация речи персонажей",
                isRecommended = true,
                badge = "🔥 Топ выбор"
            ),
            AiModelCatalogItem(
                id = "openai/gpt-audio-mini",
                name = "OpenAI: GPT Audio Mini",
                provider = AiProviderType.OPENROUTER,
                category = AiModelCategory.TTS,
                contextWindow = null,
                promptPrice = "$0.002 / 1M токенов",
                completionPrice = null,
                description = "Быстрый лёгкий синтез речи",
                isRecommended = true,
                badge = "⚡ Эконом"
            ),
            AiModelCatalogItem(
                id = "google/lyria-3-pro-preview",
                name = "Google: Lyria 3 Pro",
                provider = AiProviderType.OPENROUTER,
                category = AiModelCategory.TTS,
                contextWindow = null,
                promptPrice = "Бесплатно",
                completionPrice = null,
                description = "Музыкальная и речевая генерация Google",
                isRecommended = true,
                badge = "🎙 Премиум"
            )
        )

        val openRouterSummary = openRouterText.map { item ->
            item.copy(
                category = AiModelCategory.SUMMARY,
                isRecommended = item.id in setOf("google/gemini-2.5-flash-lite", "openai/gpt-4o-mini", "meta-llama/llama-3.3-70b-instruct")
            )
        }

        catalogCache["${AiProviderType.OPENROUTER.name}:${AiModelCategory.TEXT.name}"] = openRouterText
        catalogCache["${AiProviderType.OPENROUTER.name}:${AiModelCategory.IMAGE.name}"] = openRouterImage
        catalogCache["${AiProviderType.OPENROUTER.name}:${AiModelCategory.TTS.name}"] = openRouterTts
        catalogCache["${AiProviderType.OPENROUTER.name}:${AiModelCategory.IMAGE_PROMPT.name}"] = openRouterText.map { it.copy(category = AiModelCategory.IMAGE_PROMPT) }
        catalogCache["${AiProviderType.OPENROUTER.name}:${AiModelCategory.SUMMARY.name}"] = openRouterSummary
    }

    fun getRecommendedModel(provider: AiProviderType, category: AiModelCategory): String {
        val models = getModels(provider, category)
        return models.firstOrNull { it.isRecommended }?.id ?: models.firstOrNull()?.id ?: ""
    }

    fun getSupportedCategories(provider: AiProviderType): Set<AiModelCategory> {
        return AiModelCategory.entries.filter { category ->
            getModels(provider, category).isNotEmpty()
        }.toSet()
    }

    private fun getDefaultModels(provider: AiProviderType, category: AiModelCategory): List<AiModelCatalogItem> {
        return emptyList()
    }
}

@Serializable
private data class OpenRouterModelsResponse(
    val data: List<OpenRouterModelItem>? = null
)

@Serializable
private data class OpenRouterModelItem(
    val id: String? = null,
    val name: String? = null,
    val description: String? = null,
    val context_length: Long? = null,
    val architecture: OpenRouterArchitecture? = null,
    val pricing: OpenRouterPricing? = null
)

@Serializable
private data class OpenRouterArchitecture(
    val modality: String? = null,
    val tokenizer: String? = null,
    val input_modalities: List<String>? = null,
    val output_modalities: List<String>? = null
)

@Serializable
private data class OpenRouterPricing(
    val prompt: String? = null,
    val completion: String? = null,
    val image: String? = null,
    val image_output: String? = null,
    val audio: String? = null,
    val audio_output: String? = null,
    val request: String? = null
)
