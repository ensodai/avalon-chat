package com.ensodai.avalon.domain.sessionWorker

import kotlinx.browser.document
import kotlinx.browser.window

// @JsFun аннотация позволяет выполнять сырой JS код и безопасно привязывать его к Kotlin.
@JsFun("() => document.visibilityState")
internal external fun getDocumentVisibilityState(): String

@JsFun("(listener) => document.addEventListener('visibilitychange', listener)")
internal external fun addVisibilityChangeListener(listener: () -> Unit)

@JsFun("""() => {
    if ('serviceWorker' in navigator) {
        navigator.serviceWorker.getRegistrations().then(function(registrations) {
            for (let registration of registrations) {
                registration.unregister();
            }
        }).finally(() => {
            window.location.reload();
        });
    } else {
        window.location.reload();
    }
}""")
internal external fun performHardResetAndReload()

