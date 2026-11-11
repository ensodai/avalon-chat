/**
 * Мост для перехвата кнопки "Назад" браузера через History API.
 * Используется AvalonBackHandler для синхронизации browser history с Compose навигацией.
 */

let popstateHandler = null;

/**
 * Пушит "sentinel" запись в browser history.
 * Это позволяет перехватить следующее нажатие "Назад" через событие popstate.
 */
export function pushHistorySentinel() {
    window.history.pushState({ avalonSentinel: true }, '');
}

/**
 * Устанавливает обработчик события popstate (кнопка "Назад").
 * @param {Function} callback Kotlin-функция, вызываемая при нажатии "Назад"
 */
export function setPopstateHandler(callback) {
    // Убираем предыдущий, если был
    if (popstateHandler) {
        window.removeEventListener('popstate', popstateHandler);
    }
    popstateHandler = function(e) {
        callback();
    };
    window.addEventListener('popstate', popstateHandler);
}

/**
 * Убирает обработчик popstate.
 */
export function clearPopstateHandler() {
    if (popstateHandler) {
        window.removeEventListener('popstate', popstateHandler);
        popstateHandler = null;
    }
}
