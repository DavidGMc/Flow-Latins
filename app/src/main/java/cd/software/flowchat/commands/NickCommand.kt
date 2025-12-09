package cd.software.flowchat.commands

import android.content.Context
import android.util.Log
import cd.software.flowchat.IRCService
import es.chat.R
import cd.software.flowchat.model.Conversation

//ya funciona correctamente

/**
 * Comando para cambiar el nickname en IRC.
 * Uso: /nick nuevoNick
 */
class NickCommand(context: Context) : BaseCommand(context) {
    override val name = "nick"
    override val aliases = listOf("nickname")
    override val description = context.getString(R.string.nick_command_description)
    override val usage = context.getString(R.string.nick_command_usage)
    override val availableInChannel = true
    override val availableInPrivate = true
    override val requiresConnection = true

    override suspend fun execute(
        service: IRCService,
        conversation: Conversation,
        args: List<String>,
        rawMessage: String
    ): CommandResult {
        if (!validateMinArgs(args, 1, context.getString(R.string.nick_error_no_nickname))) {
            return error(context.getString(R.string.nick_error_usage, usage))
        }

        val newNick = args[0]

        try {
            service.changeNick(newNick)
            return success(context.getString(R.string.nick_success_message, newNick))
        } catch (e: Exception) {
            return error(context.getString(R.string.nick_error_change, e.message ?: ""))
        }
    }
}