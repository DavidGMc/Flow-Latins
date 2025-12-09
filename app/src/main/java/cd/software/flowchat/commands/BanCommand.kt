package cd.software.flowchat.commands

import android.content.Context
import cd.software.flowchat.IRCService
import cd.software.flowchat.model.Conversation
import cd.software.flowchat.model.ConversationType
import es.chat.R

/**
 * Comando para banear a un usuario de un canal IRC.
 * Uso: /ban <nick|hostmask> o /ban #canal <hostmask>
 * 
 * Requiere permisos de operador en el canal.
 */
class BanCommand(context: Context) : BaseCommand(context) {
    override val name = "ban"
    override val aliases = listOf("b")
    override val description = context.getString(R.string.ban_command_description)
    override val usage = context.getString(R.string.ban_command_usage)
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
        if (!validateMinArgs(args, 1, context.getString(R.string.ban_error_no_hostmask))) {
            return error(context.getString(R.string.ban_error_usage, usage))
        }

        try {
            // Determinar canal y hostmask
            val (channel, hostmask) = when {
                // Si primer argumento es un canal: /ban #canal hostmask
                args.size > 1 && args[0].startsWith("#") -> {
                    args[0] to args[1]
                }
                // Si estamos en un canal: /ban hostmask
                conversation.type == ConversationType.CHANNEL -> {
                    conversation.name to args[0]
                }
                // Error: necesita especificar canal
                else -> {
                    return error(context.getString(R.string.ban_error_no_channel))
                }
            }

            // Validar permisos de operador
            if (!PermissionValidator.hasOpPermission(service, channel)) {
                return error(context.getString(R.string.error_no_permission))
            }

            // Ejecutar comando
            service.banUser(channel, hostmask)
            return success(context.getString(R.string.ban_success, hostmask, channel))
            
        } catch (e: Exception) {
            return error(context.getString(R.string.ban_error_general, e.message ?: ""))
        }
    }
}
