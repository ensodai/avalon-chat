package app.application.chat

interface ChatCommandHandler<T : ChatCommand> {
    suspend fun handle(
        actor: ChatActor,
        command: T,
        context: ChatCommandContext
    )
}
