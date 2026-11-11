package app.service

import app.utils.EnvProvider
import app.utils.logger.LlmLogger
import com.google.genai.Client
import com.google.genai.types.ClientOptions
import com.google.genai.types.ProxyOptions
import com.google.genai.types.ProxyType

/**
 * Подключает HTTP-прокси к Gemini Client.Builder, если задан хост US_PROXY_HOST.
 */
fun Client.Builder.withConfiguredProxy(): Client.Builder {
    val host = EnvProvider.getUSProxyHost()
    if (host.isBlank()) {
        return this
    }

    val port = EnvProvider.getUSProxyPort()
    val user = EnvProvider.getUSProxyUser()
    val pass = EnvProvider.getUSProxyPass()

    val proxyBuilder = ProxyOptions.builder()
        .type(ProxyType.Known.HTTP)
        .host(host)
        .port(port)

    if (user.isNotBlank() && pass.isNotBlank()) {
        proxyBuilder.username(user).password(pass)
    }

    val clientOptions = ClientOptions.builder()
        .proxyOptions(proxyBuilder.build())
        .build()

    clientOptions(clientOptions)
    LlmLogger.inf("GeminiProxy", "Connected via US Proxy: $host:$port")
    return this
}
