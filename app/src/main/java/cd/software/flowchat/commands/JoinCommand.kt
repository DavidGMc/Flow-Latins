package cd.software.flowchat.commands

import android.content.Context
import cd.software.flowchat.IRCService
import cd.software.flowchat.irc.conversation.ConversationManager
import es.chat.R
import cd.software.flowchat.model.Conversation

/**
 * Comando para unirse a un canal IRC.
 * Uso: /join #canal [contraseña]
 */
class JoinCommand(context: Context) : BaseCommand(context) {
    override val name = "join"
    override val aliases = listOf("j")
    override val description = context.getString(R.string.join_command_description)
    override val usage = context.getString(R.string.join_command_usage)
    override val availableInChannel = true
    override val availableInPrivate = true
    override val requiresConnection = true

    override suspend fun execute(
        service: IRCService,
        conversation: Conversation,
        args: List<String>,
        rawMessage: String
    ): CommandResult {
        if (!validateMinArgs(args, 1, context.getString(R.string.join_error_no_channel))) {
            return error(context.getString(R.string.join_error_usage, usage))
        }

        val channel = args[0]

        // Validar que el nombre del canal sea válido
        if (!channel.startsWith("#")) {
            return error(context.getString(R.string.join_error_invalid_channel))
        }

        // Si hay una contraseña, unirse con ella
        val password = if (args.size > 1) args[1] else null

        try {
            if (password != null) {
                service.bot?.sendRaw()?.rawLine("JOIN $channel $password")
            } else {
                service.joinChannel(channel)
            }
            return success(context.getString(R.string.join_success_message, channel))
        } catch (e: Exception) {
            return error(context.getString(R.string.join_error_general, e.message ?: ""))
        }
    }
}