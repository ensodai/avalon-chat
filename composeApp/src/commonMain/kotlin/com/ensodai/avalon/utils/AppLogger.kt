package com.ensodai.avalon.utils

import co.touchlab.kermit.Logger

interface AppLogger {
    fun v(tag: String, message: Any)
    fun d(tag: String, message: Any)
    fun i(tag: String, message: Any)
    fun w(tag: String, message: Any)
    fun e(tag: String, message: Any)
}

class KermitLogger : AppLogger {
    override fun v(tag: String, message: Any) {
        if (!isAppInDebugMode()) return
        Logger.withTag(tag).v { message.toString() }
    }

    override fun d(tag: String, message: Any) {
        if (!isAppInDebugMode()) return
        Logger.withTag(tag).d { message.toString() }
    }

    override fun i(tag: String, message: Any) {
        if (!isAppInDebugMode()) return
        Logger.withTag(tag).i { message.toString() }
    }

    override fun w(tag: String, message: Any) {
        if (!isAppInDebugMode()) return
        Logger.withTag(tag).w { message.toString() }
    }

    override fun e(tag: String, message: Any) {
        if (!isAppInDebugMode()) return
        Logger.withTag(tag).e { message.toString() }
    }
}

object L {
    private var logger: AppLogger = KermitLogger()

    fun init(logger: AppLogger) {
        this.logger = logger
    }

    fun get(): AppLogger = logger

    fun v(tag: String, message: Any) {
        if (!isAppInDebugMode()) return
        logger.v(tag, message)
    }

    fun d(tag: String, message: Any) {
        if (!isAppInDebugMode()) return
        logger.d(tag, message)
    }

    fun i(tag: String, message: Any) {
        if (!isAppInDebugMode()) return
        logger.i(tag, message)
    }

    fun w(tag: String, message: Any) {
        if (!isAppInDebugMode()) return
        logger.w(tag, message)
    }

    fun e(tag: String, message: Any) {
        if (!isAppInDebugMode()) return
        logger.e(tag, message)
    }
}
