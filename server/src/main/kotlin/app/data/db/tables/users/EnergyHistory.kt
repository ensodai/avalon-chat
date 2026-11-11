package app.data.db.tables.users

import app.core.BaseUuidTable
import java.math.BigDecimal

// Перечисление причин изменения баланса
enum class EnergyOperationType {
    // --- РАСХОДЫ (Spends) ---
    SPEND_CHAT_REPLY,      // Обычный ответ (на сообщение юзера)
    SPEND_CHAT_FORCE,      // Форсированная генерация (кнопка "продолжить/напиши")
    SPEND_CHAT_REGEN,      // Регенерация (свайп сообщения)
    SPEND_LIVE_CALL_TURN,  // Один завершённый live-call turn
    SPEND_GIFT,            // Отправка подарка (будущая фича)
    SPEND_CHAT_TTS,        // Озвучивание текста (TTS)
    SPEND_PHOTO_UNLOCK,    // Разблокировка приватного фото персонажа

    // --- ДОХОДЫ / СИСТЕМНОЕ ---
    DAILY_RESET_GRANT,     // Начисление дневной нормы
    DAILY_ROLLOVER,        // Перенос остатка
    PURCHASE_PACK,         // Покупка пакета
    SUBSCRIPTION_GRANT,    // Бонус подписки
    ADMIN_ADJUSTMENT,      // Админка
    REFUND_ERROR,          // Возврат
    REGISTRATION_BONUS,    // Бонус регистрации
    
    // --- НАЧИСЛЕНИЯ АВТОРАМ ---
    ACCUMULATE_BONUS,      // Начисление 20% в буфер автора
    CLAIM_BONUS,           // Клейм бонуса автором на личный баланс

    // --- LLM TOKEN USAGE ---
    LLM_TOKEN_USAGE        // Детальная статистика по токенам (prompt/output/cached)
}

object EnergyHistory : BaseUuidTable("energy_history") {
    // Ссылка на юзера
    val user = reference("user_id", AppUsers).index("idx_energy_history_user")

    // Тип операции
    val operationType = enumerationByName("operation_type", 30, EnergyOperationType::class)

    // Изменение баланса.
    // Отрицательное число = списание (-15), Положительное = начисление (+100)
    val amount = integer("amount")

    // --- СЛЕПКИ БАЛАНСОВ ПОСЛЕ ОПЕРАЦИИ ---
    // Важно записывать, сколько стало денег у юзера в обоих карманах
    // чтобы не пересчитывать всю историю с нуля для проверки.
    val dailyBalanceAfter = integer("daily_balance_after")
    val reserveBalanceAfter = integer("reserve_balance_after")

    // Метаданные (контекст).
    // Тут можно хранить JSON или просто строку, например:
    // "model:gpt-4; tokens:150" или "purchase_id: xxx"
    val metaData = text("meta_data").nullable()

    // Ссылка на сообщение (если это генерация).
    // Делаем nullable, так как не все операции связаны с сообщениями.
    // Если сообщение удалят, лог должен остаться, поэтому лучше не делать жесткий FK
    // или делать ON DELETE SET NULL, но лучше просто хранить ID как строку или UUID без констрейнта,
    // если история важнее целостности ссылок.
    val relatedMessageId = uuid("related_message_id").nullable()

    // --- ДЕТАЛЬНАЯ СТАТИСТИКА ПО ТОКЕНАМ LLM ---
    // Заполняется только для операций LLM_TOKEN_USAGE и SPEND_*
    val totalTokens = long("total_tokens").nullable()
    val promptTokens = long("prompt_tokens").nullable()
    val outputTokens = long("output_tokens").nullable()
    val cachedTokens = long("cached_tokens").nullable()
    val thoughtsTokens = long("thoughts_tokens").nullable()

    // --- РЕАЛЬНАЯ СТОИМОСТЬ В ДОЛЛАРАХ ---
    val realCostUsd = decimal("real_cost_usd", 10, 6).default(BigDecimal("0.000000"))

}
