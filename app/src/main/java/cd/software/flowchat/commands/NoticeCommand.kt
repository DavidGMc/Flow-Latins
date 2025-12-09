import android.content.Context
import cd.software.flowchat.IRCService
import es.chat.R
import cd.software.flowchat.commands.BaseCommand
import cd.software.flowchat.commands.CommandResult
import cd.software.flowchat.model.Conversation
import cd.software.flowchat.model.EventColorType
import cd.software.flowchat.model.IRCMessage
import cd.software.flowchat.model.MessageType

// ya funciona correctamente.
class NoticeCommand(context: Context) : BaseCommand(context) {
    override val name = "Notice"
    override val description = context.getString(R.string.notice_command_description)
    override val usage = context.getString(R.string.notice_command_usage)
    override val aliases = listOf("not")
    override val availableInChannel = true
    override val availableInPrivate = true
    override val requiresConnection = true

    override suspend fun execute(
        service: IRCService,
        conversation: Conversation,
        args: List<String>,
        rawMessage: String
    ): CommandResult {
        if (!validateMinArgs(args, 2, context.getString(R.string.notice_error_specify_target_message))) {
            return error(context.getString(R.string.notice_error_usage, usage))
        }

        val target = args[0]
        val message = args.drop(1).joinToString(" ")

        try {
            // Pasar el nombre del canal actual para mostrar el mensaje allí
            service.sendNotice(target, message, conversation.name)

            return success(context.getString(R.string.notice_success_sent, target))
        } catch (e: Exception) {
            return error(context.getString(R.string.notice_error_send, e.message ?: ""))
        }
    }
}