package app.service.rarityMessageService

import com.ensodai.shared.avalon.chat.models.Rarity

/**
 * Определяет профиль вероятностей для системы редкости.
 *
 * @param pityThreshold Количество "неудачных" попыток подряд, после которого гарантированно выдается награда.
 * @param pityReward Редкость, которая гарантированно выдается при срабатывании "системы жалости".
 */
enum class RarityProfile(
    internal val pityThreshold: Int,
    internal val pityReward: Rarity = Rarity.RARE
) {
    /**
     * Сбалансированный профиль, как в классических RPG.
     * Редкие вещи выпадают регулярно, но не слишком часто.
     * COMMON: 65%, UNCOMMON: 20%, RARE: 10%, EPIC: 4%, MYTHIC: 1%
     */
    CLASSIC_RPG(pityThreshold = 15),

    /**
     * Профиль, имитирующий "гача"-игры.
     * Большинство ответов обычные, что делает получение редкого ответа настоящим событием.
     * COMMON: 84.5%, UNCOMMON: 10%, RARE: 3%, EPIC: 2%, MYTHIC: 0.5%
     */
    GACHA(pityThreshold = 30); // Порог выше, т.к. система более "суровая"

    /**
     * Внутренняя функция для получения редкости на основе случайного числа.
     * @param chance Случайное число от 0.0 до 100.0
     */
    internal fun getRarityForChance(chance: Double): Rarity {
        return when (this) {
            CLASSIC_RPG -> when {
                chance < 1.0  -> Rarity.MYTHIC   // 1%
                chance < 5.0  -> Rarity.EPIC     // 4%
                chance < 15.0 -> Rarity.RARE     // 10%
                chance < 35.0 -> Rarity.UNCOMMON // 20%
                else          -> Rarity.COMMON    // 65%
            }
            GACHA -> when {
                chance < 0.5  -> Rarity.MYTHIC   // 0.5%
                chance < 2.5  -> Rarity.EPIC     // 2%
                chance < 5.5  -> Rarity.RARE     // 3%
                chance < 15.5 -> Rarity.UNCOMMON // 10%
                else          -> Rarity.COMMON    // 84.5%
            }
        }
    }
}
