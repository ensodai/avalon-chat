package app.data.db.tables
import com.ensodai.shared.avalon.gifts.models.*

import app.core.BaseUuidTable
import com.ensodai.shared.avalon.gifts.models.PremiumStatus
import java.math.BigDecimal

enum class LlmProvider {
    GOOGLE,
    OPENAI,
    ANTHROPIC,
    LOCAL,
    DEEPSEEK
}

object LlmModels : BaseUuidTable("llm_models") {
    // Уникальный код для клиента (например, "gemini_fast")
    val code = varchar("code", 50).uniqueIndex()

    // Красивое название для UI ("Gemini Flash 1.5")
    val name = varchar("name", 100)

    // Реальный ID для API провайдера ("gemini-1.5-flash-001")
    val realModelId = varchar("real_model_id", 100)

    // Провайдер (GOOGLE, OPENAI, ANTHROPIC, LOCAL) - чтобы знать, в какой сервис стучаться
    val provider = enumerationByName("provider", 50, LlmProvider::class)

    // Стоимость одного запроса (или токена, если усложнять)
    val energyCost = integer("energy_cost").default(1)

    // Минимальный тариф для доступа (FREE, PREMIUM...)
    val minStatus = enumerationByName("min_status", 20, PremiumStatus::class).default(PremiumStatus.FREE)

    // Включена ли модель (можно временно выключить, если API упал)
    val isActive = bool("is_active").default(true)

    // Сортировка в списке (чтобы первыми шли популярные)
    val sortOrder = integer("sort_order").default(0)

    // === ЦЕНЫ ДЛЯ РАСЧЁТА РЕАЛЬНОЙ СТОИМОСТИ (USD) ===
    // Цена за 1 миллион входных токенов. Для Imagen - цена за 1 изображение.
    val pricePerMillionInput = decimal("price_per_million_input", 10, 6).default(BigDecimal("0.000000"))

    // Цена за 1 миллион выходных токенов. Для Imagen = 0.
    val pricePerMillionOutput = decimal("price_per_million_output", 10, 6).default(BigDecimal("0.000000"))
}

data class LlmConfig(
    val code: String,
    val name: String,
    val modelAPIID: String,
    val provider: LlmProvider,
    val energyCost: Int,
    val minStatus: PremiumStatus,
    val isActive: Boolean
)
