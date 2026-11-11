package com.ensodai.avalon.presentation.chatScreen.component.explosionEffect

data class ExplosionConfig(
    // === ГЛАВНЫЕ НАСТРОЙКИ ===
    val durationMs: Int = 1200,          // Длительность всей анимации
    val particleCount: Int = 20,         // Количество частиц

    // === ФИЗИКА РАЗЛЕТА (в пикселях) ===
    // Высота полета (мин..макс). Чем больше, тем выше летят.
    val verticalRange: ClosedFloatingPointRange<Float> = 250f..450f,

    // Ширина разлета (мин..макс). Увеличил, как ты просил.
    val horizontalRange: ClosedFloatingPointRange<Float> = -500f..500f,

    // === ВНЕШНИЙ ВИД ===
    // Размер частиц (множитель размера шрифта). 1.0 = исходный размер.
    val startScaleRange: ClosedFloatingPointRange<Float> = 0.7f..1.3f,
    val endScale: Float = 0.5f, // К чему стремится размер (0 - исчезает в точку)

    // === ВРАЩЕНИЕ ===
    // Скорость вращения.
    // например, -15f..15f
    val rotationSpeedRange: ClosedFloatingPointRange<Float> = 0f..0f,

    // === ТАЙМИНГ ===
    // Разброс задержки старта (чтобы вылетали не одновременно)
    val startDelayRange: ClosedFloatingPointRange<Float> = 0f..0.15f,

    // Какую часть времени (0.0 - 1.0) частица появляется
    val fadeInPct: Float = 0.1f, // 10% времени на появление

    // В какой момент (0.0 - 1.0) начинать исчезать
    // Раньше было хардкодом 0.7. Если поставишь 0.4, она начнет исчезать раньше и плавнее.
    val fadeOutStartPct: Float = 0.4f,

    // === НОВЫЙ ПАРАМЕТР: СИЛА НАЧАЛЬНОГО ИМПУЛЬСА ===
    // 1.0f = равномерный старт.
    // 2.0f - 5.0f = резкий взрывной старт.
    // Рекомендую ставить от 1.5f до 3.0f для сочного эффекта.
    val initialImpulse: Float = 1.4f
)
