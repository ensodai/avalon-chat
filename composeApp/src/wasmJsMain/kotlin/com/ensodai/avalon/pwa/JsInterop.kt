@file:JsModule("./pwa-register.js")
package com.ensodai.avalon.pwa

import kotlin.js.Promise
import kotlin.js.JsAny

// Декларации внешних функций из нашего JavaScript моста
external fun initializePwaBridge(
    onUpdateAvailable: () -> Unit,
    onInstallPromptAvailable: () -> Unit
)

external fun applyPwaUpdate()

// Возвращаемый тип Promise<JsAny> обеспечивает совместимость с Wasm
external fun promptPwaInstall(): Promise<JsAny>

external fun isIosDevice(): Boolean
external fun isStandaloneMode(): Boolean
external fun hardResetPwa()

