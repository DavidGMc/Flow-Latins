package cd.software.flowchat.commands

import android.content.Context
import cd.software.flowchat.IRCService
import cd.software.flowchat.irc.conversation.ConversationManager
import cd.software.flowchat.model.Conversation
import cd.software.flowchat.model.ConversationType
import cd.software.flowchat.model.IRCMessage
import cd.software.flowchat.model.MessageType

class ChanServCommand(context: Context) : BaseCommand(context) {
    override val name = "chanserv"
    override val description = "Envía comandos a ChanServ"
    override val usage = "/chanserv <comando> [parámetros] - Ejemplo: /chanserv help"
    override val aliases = listOf("cs", "chan")
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
            return error("Debes especificar un comando para ChanServ. Uso: $usage")
        }

        // Construir el comando completo para ChanServ
        val chanservCommand = args.joinToString(" ")

        try {
            // Asegurar que exista una conversación con ChanServ
            service.startServiceConversation("ChanServ")

            // Enviar el comando como PRIVMSG a ChanServ
            val result = sendRawCommand(service, "PRIVMSG ChanServ :$chanservCommand")

            // Registrar el comando enviado en la conversación con ChanServ
            service.addMessage(
                IRCMessage(
                    sender = service.bot?.nick ?: "Me",
                    content = chanservCommand,
                    conversationType = ConversationType.SERVICE,
                    type = MessageType.TEXT,
                    isOwnMessage = true,
                    channelName = "ChanServ"
                )
            )

            return success("Comando enviado a ChanServ", showInChat = false, handledLocally = true)
        } catch (e: Exception) {
            return error("Error al enviar comando a ChanServ: ${e.message}")
        }
    }
}