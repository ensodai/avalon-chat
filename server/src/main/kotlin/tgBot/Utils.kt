package tgBot

import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment

fun CommandHandlerEnvironment.isAdmin(id: Long): Boolean {
     return this.message.chat.id == id
}

