package cd.software.flowchat.commands

import android.content.Context
import cd.software.flowchat.IRCService
import cd.software.flowchat.model.Conversation
import cd.software.flowchat.model.ConversationType
import cd.software.flowchat.model.IRCMessage
import cd.software.flowchat.model.MessageType

class NickServCommand(context: Context) : BaseCommand(context) {
    override val name = "nickserv"
    override val description = "Envía comandos a NickServ"
    override val usage = "/nickserv <comando> [parámetros] - Ejemplo: /nickserv identify tucontraseña"
    override val aliases = listOf("ns", "nick")
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
            return error("Debes especificar un comando para NickServ. Uso: $usage")
        }

        // Construir el comando completo para NickServ
        val nickservCommand = args.joinToString(" ")

        try {
            // Asegurar que exista una conversación con NickServ
            service.startServiceConversation("NickServ")

            // Enviar el comando como PRIVMSG a NickServ
            val result = sendRawCommand(service, "PRIVMSG NickServ :$nickservCommand")

            // Registrar el comando enviado en la conversación con NickServ
            service.addMessage(
                IRCMessage(
                    sender = service.bot?.nick ?: "Me",
                    content = nickservCommand,
                    conversationType = ConversationType.SERVICE,
                    type = MessageType.TEXT,
                    isOwnMessage = true,
                    channelName = "NickServ"
                )
            )

            return success("Comando enviado a NickServ", showInChat = false, handledLocally = true)
        } catch (e: Exception) {
            return error("Error al enviar comando a NickServ: ${e.message}")
        }
    }
}