package cd.software.flowchat.commands

import android.content.Context
import cd.software.flowchat.IRCService
import cd.software.flowchat.model.Conversation
import cd.software.flowchat.model.ConversationType
import es.chat.R

/**
 * Comando para quitar voz a un usuario en un canal IRC.
 * Uso: /devoice <nick> o /devoice #canal <nick>
 * 
 * Requiere permisos de operador en el canal.
 */
class DevoiceCommand(context: Context) : BaseCommand(context) {
    override val name = "devoice"
    override val aliases = listOf("dv")
    override val description = context.getString(R.string.devoice_command_description)
    override val usage = context.getString(R.string.devoice_command_usage)
    override val availableInChannel = true
    override val availableInPrivate = true
    override val requiresConnection = true

    override suspend fun execute(
        service: IRCService,
        conversation: Conversation,
        args: List<String>,
        rawMessage: String
    ): CommandResult {
        // Validar que haya al menos un argumento
        if (!validateMinArgs(args, 1, context.getString(R.string.devoice_error_no_nick))) {
            return error(context.getString(R.string.devoice_error_usage, usage))
        }

        try {
            // Determinar canal y nickname
            val (channel, nick) = when {
                // Si primer argumento es un canal: /devoice #canal nick
                args.size > 1 && args[0].startsWith("#") -> {
                    args[0] to args[1]
                }
                // Si estamos en un canal: /devoice nick
                conversation.type == ConversationType.CHANNEL -> {
                    conversation.name to args[0]
                }
                // Error: necesita especificar canal
                else -> {
                    return error(context.getString(R.string.devoice_error_no_channel))
                }
            }

            // Validar permisos de operador
            if (!PermissionValidator.hasOpPermission(service, channel)) {
                return error(context.getString(R.string.error_no_permission))
            }

            // Ejecutar comando
            service.devoiceUser(channel, nick)
            return success(context.getString(R.string.devoice_success, nick, channel))
            
        } catch (e: Exception) {
            return error(context.getString(R.string.devoice_error_general, e.message ?: ""))
        }
    }
}
