package com.ensodai.avalon.domain.models.chat

sealed class RpgMessageBlock {
    abstract val text: String

    data class Plain(override val text: String) : RpgMessageBlock()
    data class Action(override val text: String) : RpgMessageBlock()
    data class Speech(override val text: String) : RpgMessageBlock()
    data class Event(override val text: String) : RpgMessageBlock()
    data class Thought(override val text: String) : RpgMessageBlock()
}
