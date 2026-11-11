package com.ensodai.avalon.data.network

@JsName("globalThis")
external val globalThis: WebGoogleAuthGlobalThis

external interface WebGoogleAuthGlobalThis {
    val flowChatGoogleAuth: FlowChatGoogleAuth?
}

external interface FlowChatGoogleAuth {
    fun renderButton(
        left: Double,
        top: Double,
        width: Double,
        height: Double,
        onMounted: () -> Unit,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    )

    fun hideButton()
}
