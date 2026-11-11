package com.ensodai.shared.avalon.relationship

object RelationshipLevelCalculator {
    const val MAX_LEVEL = 65

    /**
     * Возвращает количество опыта, необходимое для перехода с указанного уровня на следующий.
     * Рассчитано на ~350 сообщений (3,500 EXP) до максимального 65 уровня.
     */
    fun getExperienceRequiredForNextLevel(currentLevel: Int): Long {
        if (currentLevel >= MAX_LEVEL) return 0L
        return (11L * currentLevel / 8L) + 10L
    }

    const val FIXED_HISTORY_SIZE = 40

    /**
     * Фиксированный размер истории сообщений (глубина памяти контекста).
     */
    fun getHistorySizeForLevel(level: Int = 1): Int = FIXED_HISTORY_SIZE

    /**
     * Возвращает коэффициент скидки на энергию для сообщений на основе уровня отношений.
     */
    fun getEnergyDiscountFactor(level: Int): Double {
        return when {
            level >= 50 -> 0.85 // 15% скидка
            level >= 25 -> 0.90 // 10% скидка
            level >= 10 -> 0.95 // 5% скидка
            else -> 1.0         // Без скидки
        }
    }
}
