package cd.software.flowchat.commands

import android.content.Context
import cd.software.flowchat.IRCService
import es.chat.R
import cd.software.flowchat.model.Conversation

class RawCommand(context: Context) : IRCCommand {
    private val context = context

    override val name = "raw"
    override val aliases = listOf("quote")
    override val description = context.getString(R.string.raw_command_description)
    override val usage = context.getString(R.string.raw_command_usage)
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
            return CommandResult(
                success = false,
                message = context.getString(R.string.raw_error_usage, usage),
                showInChat = true,
                handledLocally = true
            )
        }

        // Extraer todo lo que está después de /raw o /quote
        val rawCommand = rawMessage.substringAfter(" ").trim()

        try {
            service.executeServerCommand(rawCommand)
            return CommandResult(
                success = true,
                message = context.getString(R.string.raw_success_command_sent, rawCommand),
                showInChat = true,
                handledLocally = true
            )
        } catch (e: Exception) {
            return CommandResult(
                success = false,
                message = context.getString(R.string.raw_error_send_command, e.message ?: context.getString(R.string.raw_error_unknown)),
                showInChat = true,
                handledLocally = true
            )
        }
    }
}