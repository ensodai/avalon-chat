@file:JsModule("./web-back-handler.js")
package com.ensodai.avalon.navigation

/**
 * JS-интероп: пушит sentinel-запись в browser history.
 */
external fun pushHistorySentinel()

/**
 * JS-интероп: устанавливает обработчик popstate (кнопка "Назад").
 */
external fun setPopstateHandler(callback: () -> Unit)

/**
 * JS-интероп: убирает обработчик popstate.
 */
external fun clearPopstateHandler()
