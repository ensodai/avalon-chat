package com.ensodai.avalon.domain.chatEvents

import com.ensodai.avalon.utils.AppLogger

class TestAppLogger : AppLogger {
    override fun v(tag: String, message: Any) { println("[$tag] [V] $message") }
    override fun d(tag: String, message: Any) { println("[$tag] [D] $message") }
    override fun i(tag: String, message: Any) { println("[$tag] [I] $message") }
    override fun w(tag: String, message: Any) { println("[$tag] [W] $message") }
    override fun e(tag: String, message: Any) { println("[$tag] [E] $message") }
}
