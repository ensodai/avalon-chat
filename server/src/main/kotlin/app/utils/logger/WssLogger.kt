package app.utils.logger

import io.github.oshai.kotlinlogging.KotlinLogging

object WssLogger {
    private val base = AppLogger(KotlinLogging.logger("WSS"), "WSS")

    fun connect(id: String) =
        base.success("Client $id connected")

    fun disconnect(id: String) =
        base.warn("Client $id disconnected")

    fun inbound(id: String, msg: String) =
        base.event("RECV", "📥", AppLogger.Ansi.BLUE, "($id) → $msg")

    fun outbound(id: String, msg: String) =
        base.event("SEND", "📤", AppLogger.Ansi.CYAN, "($id) ← $msg")

    fun fail(msg: String) =
        base.fail("Error → $msg")

    fun info(msg: String) = base.info(msg)
}
