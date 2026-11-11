package com.ensodai.avalon.navigation

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Глобальный провайдер навигационного контроллера.
 *
 * Мы используем [staticCompositionLocalOf] вместо `compositionLocalOf`, потому что:
 * 1. Экземпляр контроллера создается один раз и (почти) никогда не меняется.
 * 2. Static-провайдер не заставляет Compose пересчитывать зависимости для всего дерева
 *    экранов, если провайдер пересоздастся — это огромная оптимизация перфоманса.
 *
 * Позволяет вызывать `LocalAvalonNavController.current.navigate()` из любой глубоко
 * вложенной кнопки без необходимости прокидывать контроллер через аргументы функций.
 */
val LocalAvalonNavController = staticCompositionLocalOf<AvalonNavController<Route>> {
    error("No AvalonNavController provided. Make sure you wrapped your UI in CompositionLocalProvider(LocalAvalonNavController provides ...)")
}
