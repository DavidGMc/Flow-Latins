package cd.software.flowchat.commands

import android.content.Context
import cd.software.flowchat.IRCService

import cd.software.flowchat.model.Conversation
import cd.software.flowchat.model.ConversationType
import es.chat.R

class OpCommand(context: Context) : BaseCommand(context) {
    override val name = "op"
    override val description = context.getString(R.string.op_command_description)
    override val usage = context.getString(R.string.op_command_usage)
    override val aliases = listOf("op")
    override val availableInChannel = true
    override val availableInPrivate = true
    override val requiresConnection = true

    override suspend fun execute(
        service: IRCService,
        conversation: Conversation,
        args: List<String>,
        rawMessage: String
    ): CommandResult {
        if (args.isEmpty()) {
            return error(context.getString(R.string.op_error_no_nickname))
        }

        try {
            // Determinar si el primer argumento es un canal
            val (channel, nick) = if (args.size > 1 && args[0].startsWith("#")) {
                args[0] to args[1]
            } else {
                // Si estamos en un canal, usamos ese canal
                if (conversation.type != ConversationType.CHANNEL) {
                    return error(context.getString(R.string.op_error_specify_channel))
                }
                conversation.name to args[0]
            }

            service.opUser(channel, nick)
            return success(context.getString(R.string.op_success_message, nick, channel))
        } catch (e: Exception) {
            return error(context.getString(R.string.op_error_general, e.message ?: ""))
        }
    }
}
