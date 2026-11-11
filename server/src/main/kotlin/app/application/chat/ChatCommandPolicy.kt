package app.application.chat

enum class ChatContextMode {
    EXISTING_CHAT_REQUIRED,
    BOOTSTRAP_ALLOWED
}

data class ChatCommandPolicy(
    val contextMode: ChatContextMode,
    val requiresCharacterMatch: Boolean
)

fun ChatCommand.policy(): ChatCommandPolicy = when (this) {
    is ChatCommand.SendMessage -> ChatCommandPolicy(
        contextMode = ChatContextMode.BOOTSTRAP_ALLOWED,
        requiresCharacterMatch = true
    )
    is ChatCommand.ForceStartGenerateResponse -> ChatCommandPolicy(
        contextMode = ChatContextMode.EXISTING_CHAT_REQUIRED,
        requiresCharacterMatch = true
    )
    is ChatCommand.GenerateNewContainer -> ChatCommandPolicy(
        contextMode = ChatContextMode.EXISTING_CHAT_REQUIRED,
        requiresCharacterMatch = true
    )
    is ChatCommand.SendUserReaction -> ChatCommandPolicy(
        contextMode = ChatContextMode.EXISTING_CHAT_REQUIRED,
        requiresCharacterMatch = true
    )
    is ChatCommand.ChangeActiveContainer,
    is ChatCommand.DeleteMessages,
    is ChatCommand.EditMessage,
    is ChatCommand.StopGenerateResponse -> ChatCommandPolicy(
        contextMode = ChatContextMode.EXISTING_CHAT_REQUIRED,
        requiresCharacterMatch = false
    )
}

fun ChatCommand.characterIdOrNull() = when (this) {
    is ChatCommand.SendMessage -> characterId
    is ChatCommand.ForceStartGenerateResponse -> characterId
    is ChatCommand.GenerateNewContainer -> characterId
    is ChatCommand.SendUserReaction -> characterId
    is ChatCommand.ChangeActiveContainer,
    is ChatCommand.DeleteMessages,
    is ChatCommand.EditMessage,
    is ChatCommand.StopGenerateResponse -> null
}
