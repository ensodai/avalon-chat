package app.application.chat

import app.domain.chat.SendMessageFlow
import org.koin.core.annotation.Single

@Single
class SendMessageChatCommandHandler(
    private val prepareSendMessageContextService: PrepareSendMessageContextService,
    private val sendMessageFlow: SendMessageFlow
) : ChatCommandHandler<ChatCommand.SendMessage> {

    override suspend fun handle(
        actor: ChatActor,
        command: ChatCommand.SendMessage,
        context: ChatCommandContext
    ) {
        val prepared = prepareSendMessageContextService.prepare(
            context = context,
            command = command
        ) ?: return

        sendMessageFlow.run(prepared)
    }
}
