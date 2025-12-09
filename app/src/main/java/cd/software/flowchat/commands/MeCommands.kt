package cd.software.flowchat.commands



import android.content.Context
import cd.software.flowchat.IRCService
import cd.software.flowchat.irc.conversation.ConversationManager
import cd.software.flowchat.model.Conversation
import cd.software.flowchat.model.ConversationType
import cd.software.flowchat.model.EventColorType
import cd.software.flowchat.model.IRCMessage
import cd.software.flowchat.model.MessageType
import es.chat.R

// ya funciona correctamente
/**
 * Comando para enviar acciones CTCP (/me).
 * Uso: /me está escribiendo código
 * Envía: PRIVMSG #canal :\u0001ACTION está escribiendo código\u0001
 */
class MeCommand(context: Context) : BaseCommand(context) {
    override val name = "me"
    override val aliases = listOf<String>()
    override val description = context.getString(R.string.me_command_description)
    override val usage = context.getString(R.string.me_command_usage)
    override val availableInChannel = true
    override val availableInPrivate = true
    override val requiresConnection = true

    override suspend fun execute(
        service: IRCService,
        conversation: Conversation,
        args: List<String>,
        rawMessage: String
    ): CommandResult {
        // Validar que haya texto para la acción
        if (args.isEmpty()) {
            return error(context.getString(R.string.me_error_usage, usage))
        }

        // Validar que estemos en un canal o conversación privada válida
        if (conversation.type == ConversationType.SERVER_STATUS ||
            conversation.type == ConversationType.SERVICE) {
            return error(context.getString(R.string.me_error_no_target))
        }

        val action = args.joinToString(" ")
        val nick = service.bot?.nick ?: context.getString(R.string.me_default_nick)

        return try {
            // Enviar acción usando la función del servicio
            service.sendAction(conversation.name, action)

            // Mostrar la acción en la conversación actual
            // Formato visual: * Nick está escribiendo código
            service.addMessage(
                IRCMessage(
                    sender = nick,
                    content = action,
                    conversationType = conversation.type,
                    type = MessageType.ACTION,
                    isOwnMessage = true,
                    channelName = conversation.name,
                    eventColorType = EventColorType.ACTION
                )
            )

            success("", showInChat = false)
        } catch (e: Exception) {
            error(context.getString(R.string.me_error_send, e.message ?: ""))
        }
    }
}