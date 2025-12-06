package cd.software.flowchat.commands

import android.content.Context
import cd.software.flowchat.IRCService
import es.chat.R
import cd.software.flowchat.model.Conversation

class WhoisCommand(context: Context) : BaseCommand(context) {
    override val name = "whois"
    override val aliases = listOf("w")
    override val description = context.getString(R.string.whois_command_description)
    override val usage = context.getString(R.string.whois_command_usage)
    override val availableInChannel = true
    override val availableInPrivate = true
    override val requiresConnection = true

    override suspend fun execute(
        service: IRCService,
        conversation: Conversation,
        args: List<String>,
        rawMessage: String
    ): CommandResult {

        if (!validateMinArgs(args, 1, context.getString(R.string.whois_error_no_nickname))) {
            return error(context.getString(R.string.whois_error_usage, usage))
        }

        val nickname = args[0]

        try {
            service.requestWhois(nickname)
            return success(context.getString(R.string.whois_success_message, nickname))
        } catch (e: Exception) {
            return error(context.getString(R.string.whois_error_request, e.message ?: ""))
        }
    }
}