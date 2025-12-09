package cd.software.flowchat.commands

import android.content.Context
import cd.software.flowchat.IRCService
import cd.software.flowchat.model.Conversation
import cd.software.flowchat.model.ConversationType
import es.chat.R

/**
 * Comando para salir de un canal IRC.
 * Uso: /part [#canal] [mensaje]
 * 
 * No requiere permisos especiales - cualquier usuario puede salir de un canal.
 */
class PartCommand(context: Context) : BaseCommand(context) {
    override val name = "part"
    override val aliases = listOf("leave")
    override val description = context.getString(R.string.part_command_description)
    override val usage = context.getString(R.string.part_command_usage)
    override val availableInChannel = true
    override val availableInPrivate = true
    override val requiresConnection = true

    override suspend fun execute(
        service: IRCService,
        conversation: Conversation,
        args: List<String>,
        rawMessage: String
    ): CommandResult {
        try {
            // Determinar canal y mensaje de despedida
            val (channel, partMessage) = when {
                // Si no hay argumentos y estamos en un canal: /part
                args.isEmpty() && conversation.type == ConversationType.CHANNEL -> {
                    conversation.name to null
                }
                // Si primer argumento es un canal: /part #canal [mensaje]
                args.isNotEmpty() && args[0].startsWith("#") -> {
                    val msg = if (args.size > 1) args.drop(1).joinToString(" ") else null
                    args[0] to msg
                }
                // Si estamos en canal y hay mensaje: /part mensaje
                args.isNotEmpty() && conversation.type == ConversationType.CHANNEL -> {
                    conversation.name to args.joinToString(" ")
                }
                // Error: necesita especificar canal
                else -> {
                    return error(context.getString(R.string.part_error_no_channel))
                }
            }

            // Ejecutar comando
            service.partChannel(channel)
            
            val successMsg = if (partMessage != null) {
                context.getString(R.string.part_success_with_message, channel, partMessage)

            } else {
                context.getString(R.string.part_success, channel)
            }
            service.removeChannelFromConversations(channel)
            return success(successMsg)
            
        } catch (e: Exception) {
            return error(context.getString(R.string.part_error_general, e.message ?: ""))
        }
    }
}
