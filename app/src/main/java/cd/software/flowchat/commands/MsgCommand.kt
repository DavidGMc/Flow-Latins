package cd.software.flowchat.commands

import android.content.Context
import cd.software.flowchat.IRCService

import cd.software.flowchat.model.Conversation
import cd.software.flowchat.model.ConversationType
import cd.software.flowchat.model.EventColorType
import cd.software.flowchat.model.IRCMessage
import cd.software.flowchat.model.MessageType
import es.chat.R

/**
 * Comando para enviar mensajes privados (/msg).
 * Uso: /msg nickserv help
 */
class MsgCommand(context: Context) : BaseCommand(context) {
    override val name = "msg"
    override val aliases = listOf("ns", "cs", "bs", "ms", "os") // Atajos para servicios comunes
    override val description = context.getString(R.string.msg_command_description)
    override val usage = context.getString(R.string.msg_command_usage)
    override val availableInChannel = true
    override val availableInPrivate = true
    override val requiresConnection = true

    override suspend fun execute(
        service: IRCService,
        conversation: Conversation,
        args: List<String>,
        rawMessage: String
    ): CommandResult {
        if (args.size < 2) {
            return error(context.getString(R.string.msg_error_usage, usage))
        }

        // Expandir alias si el comando vino como /ns, /cs, etc.
        val rawTarget = args[0]
        val target = when (rawTarget.lowercase()) {
            "ns" -> "NickServ"
            "cs" -> "ChanServ"
            "bs" -> "BotServ"
            "ms" -> "MemoServ"
            "os" -> "OperServ"
            else -> rawTarget
        }

        val message = args.drop(1).joinToString(" ")

        // Crear conversación temporal con el destino real
        val tempConversation = Conversation(
            name = target,
            type = ConversationType.PRIVATE_MESSAGE
        )

        return try {
            // Enviar el mensaje real
            service.sendMessage(tempConversation, message)

            // Reflejar el mensaje también en la conversación actual
            service.addMessage(

                IRCMessage(
                    sender = service.bot?.nick ?: context.getString(R.string.msg_default_sender),
                    content = context.getString(R.string.msg_sent_indicator, target, message),
                    conversationType = conversation.type,
                    type = MessageType.TEXT,
                    isOwnMessage = true,
                    channelName = conversation.name,
                    eventColorType = EventColorType.ERROR
                )
            )

            success(context.getString(R.string.msg_success_sent, target))
        } catch (e: Exception) {
            error(context.getString(R.string.msg_error_send, target, e.message ?: ""))
        }
    }
}