package com.ensodai.avalon.domain.sessionWorker

import kotlinx.browser.window

class WebSessionPlatformBridge(
    private val sessionWorker: SessionWorker,
    private val databaseHealthChecker: DatabaseHealthChecker
) {
    private var started = false

    fun start() {
        if (started) return
        started = true

        window.addEventListener("online", {
            sessionWorker.onNetworkAvailable()
        })

        // Интеграция с API видимости страницы
        addVisibilityChangeListener {
            val state = getDocumentVisibilityState()
            if (state == "visible") {
                // 1. Инициируем проверку жизнеспособности базы данных OPFS.
                databaseHealthChecker.pingDatabaseOnWakeUp()
                
                // 2. Если мы проснулись, также проверяем наличие сети для синхронизации,
                // так как за время нахождения в фоне устройство могло сменить точку доступа WiFi.
                if (window.navigator.onLine) {
                    sessionWorker.onNetworkAvailable()
                }
            }
        }

        if (window.navigator.onLine) {
            sessionWorker.onNetworkAvailable()
        }
    }
}
