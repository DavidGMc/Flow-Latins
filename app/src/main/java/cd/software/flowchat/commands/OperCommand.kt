package cd.software.flowchat.commands

import android.content.Context
import cd.software.flowchat.IRCService
import es.chat.R
import cd.software.flowchat.model.Conversation

class OperCommand(context: Context) : BaseCommand(context) {
    override val name = "oper"
    override val aliases = listOf("oper")
    override val description = context.getString(R.string.oper_command_description)
    override val usage = context.getString(R.string.oper_command_usage)
    override val availableInChannel = true
    override val availableInPrivate = true
    override val requiresConnection = true

    override suspend fun execute(
        service: IRCService,
        conversation: Conversation,
        args: List<String>,
        rawMessage: String
    ): CommandResult {
        // Validar que haya exactamente 2 argumentos
        if (args.size != 2) {
            return error(context.getString(R.string.oper_error_usage, usage))
        }

        val nickname = args[0]
        val password = args[1]

        try {
            // Usamos el método sendRawMessage que ya existe en tu IRCService
            service.executeServerCommand("OPER", "$nickname $password")
            return success(context.getString(R.string.oper_success_request, nickname))
        } catch (e: Exception) {
            return error(context.getString(R.string.oper_error_request, e.message ?: ""))
        }
    }
}
