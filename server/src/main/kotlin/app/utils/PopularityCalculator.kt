package app.utils

import kotlin.math.ln
import kotlin.math.pow
import kotlin.time.Instant
import kotlin.time.Clock

object PopularityCalculator {

    /**
     * Вычисляет Trend Score (рейтинг популярности) для персонажа.
     *
     * Формула:
     * Score = (ln(Users + 1) * 10 + ln(Messages + 1)) / (T_hours + 2)^1.5 + Boost_new
     *
     * Детальное описание работы формулы:
     * 1. **Логарифмическая шкала активности (ln(Users + 1) и ln(Messages + 1)):**
     *    Сглаживает лавинообразный рост. Один гиперактивный пользователь, написавший 10 000 сообщений
     *    одному боту, не сможет искусственно поднять его в топ выше качественного бота, у которого
     *    100 разных собеседников по 10 сообщений. Логарифм нивелирует накрутку.
     *
     * 2. **Приоритет уникальных пользователей (коэффициент 10.0):**
     *    Количество уникальных пользователей (установивших связь в `UserCharacterRelationships`) имеет 
     *    в 10 раз больший вес, чем суммарное количество сообщений. Бот, интересный многим, ценится выше.
     *
     * 3. **Экспоненциальное затухание (знаменатель (T_hours + 2)^1.5):**
     *    Рейтинг штрафуется со временем, прошедшим с момента последнего сообщения (T_hours).
     *    Если боту долго никто не пишет, T_hours растет, знаменатель увеличивается, и рейтинг падает.
     *    Показатель степени `1.5` управляет скоростью остывания (сбалансировано для остывания в течение нескольких дней).
     *    Константа `+ 2.0` исключает деление на ноль при мгновенной активности (T_hours ≈ 0) и предотвращает 
     *    аномальные пики рейтинга.
     *
     * 4. **Фактор новизны (Boost_new):**
     *    Новым ботам (созданным менее 7 дней назад) начисляется фиксированный бонус `+5.0` к рейтингу.
     *    Это дает новичкам временный буст, позволяя им выйти в топ категории "new" и "all", чтобы
     *    заявить о себе и привлечь первых пользователей.
     */
    fun calculate(
        usersCount: Long,
        messageCount: Long,
        lastInteractionAt: Instant,
        createdAt: Instant,
        now: Instant = Clock.System.now()
    ): Double {
        val users = usersCount.toDouble()
        val messages = messageCount.toDouble()
        
        // Вычисляем T в часах
        val durationSinceLastInteraction = now - lastInteractionAt
        val tHours = durationSinceLastInteraction.inWholeMinutes.toDouble() / 60.0
        
        // Знаменатель (T_hours + 2)^1.5
        val denominator = (tHours + 2.0).pow(1.5)
        
        // Boost для новинок (если создан менее 7 дней назад)
        val durationSinceCreation = now - createdAt
        val isNew = durationSinceCreation.inWholeDays < 7
        val boost = if (isNew) 5.0 else 0.0
        
        // Итоговая формула
        val score = (ln(users + 1.0) * 10.0 + ln(messages + 1.0)) / denominator + boost
        
        return score
    }
}
