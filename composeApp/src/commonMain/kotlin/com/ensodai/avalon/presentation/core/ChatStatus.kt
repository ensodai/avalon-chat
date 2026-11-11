package com.ensodai.avalon.presentation.core

import com.ensodai.shared.avalon.chat.models.Rarity

sealed interface ChatStatus {

    object Connecting : ChatStatus    // Красный / "Соединение..."

    object Updating : ChatStatus      // Желтый / "Обновление..."

    data class Typing(val rarity: Rarity) : ChatStatus        // Зеленый / "Печатает..."

    object Online : ChatStatus        // Синий / "В сети"

}
