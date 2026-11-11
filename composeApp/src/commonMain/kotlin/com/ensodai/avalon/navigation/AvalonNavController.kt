package com.ensodai.avalon.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.ensodai.avalon.utils.L

/**
 * Логический контроллер навигации.
 * Служит фасадом для [NavBackStack] из библиотеки navigation3.
 * Управляет ИСКЛЮЧИТЕЛЬНО логикой (какие экраны в стеке), не зная о визуальном состоянии,
 * анимациях или свайпах.
 *
 * @param backStack Реактивный стек маршрутов.
 */
class AvalonNavController<T : NavKey>(
    val backStack: NavBackStack<T>
) {
    /** Флаг, показывающий, можно ли сделать шаг назад (есть ли экраны под текущим) */
    val canPop: Boolean get() = backStack.size > 1

    /**
     * Добавляет новый экран в стек.
     * Оптимизировано от "двойных кликов": не позволяет запушить тот же экран,
     * если он уже находится на вершине стека.
     */
    fun navigate(route: T) {
        L.i("Test nav", "navigate to screen = $route, from screen = ${backStack.last()}")
        if (backStack.isNotEmpty() && backStack.last() == route) return
        backStack.add(route)
    }

    /**
     * Удаляет верхний экран из стека (возврат назад).
     */
    fun pop() {
        L.i("Test nav", "pop")
        if (canPop) backStack.removeAt(backStack.size - 1)
    }

    /**
     * Полностью заменяет стек одним маршрутом.
     * Нужен для смены root-flow (splash/login/main) без platform-specific навигаторов.
     */
    fun resetTo(route: T) {
        L.i("Test nav", "reset stack to $route")
        backStack.clear()
        backStack.add(route)
    }
}

/**
 * Создает и запоминает экземпляр [AvalonNavController], привязанный к жизненному циклу Compose.
 */
@Composable
fun <T : NavKey> rememberAvalonNavController(
    backStack: NavBackStack<T>
): AvalonNavController<T> {
    return remember(backStack) { AvalonNavController(backStack) }
}



