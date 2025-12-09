package cd.software.flowchat.commands

import android.content.Context
import cd.software.flowchat.IRCService
import cd.software.flowchat.model.Conversation
import cd.software.flowchat.model.ConversationType
import es.chat.R

/**
 * Comando para expulsar (kick) a un usuario de un canal IRC.
 * Uso: /kick <nick> [razón] o /kick #canal <nick> [razón]
 * 
 * Requiere permisos de operador en el canal.
 */
class KickCommand(context: Context) : BaseCommand(context) {
    override val name = "kick"
    override val aliases = listOf("k")
    override val description = context.getString(R.string.kick_command_description)
    override val usage = context.getString(R.string.kick_command_usage)
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
        if (!validateMinArgs(args, 1, context.getString(R.string.kick_error_no_nick))) {
            return error(context.getString(R.string.kick_error_usage, usage))
        }

        try {
            // Determinar canal, nickname y razón
            val (channel, nick, reason) = when {
                // Si primer argumento es un canal: /kick #canal nick [razón]
                args.size >= 2 && args[0].startsWith("#") -> {
                    val kickReason = if (args.size > 2) args.drop(2).joinToString(" ") else null
                    Triple(args[0], args[1], kickReason)
                }
                // Si estamos en un canal: /kick nick [razón]
                conversation.type == ConversationType.CHANNEL -> {
                    val kickReason = if (args.size > 1) args.drop(1).joinToString(" ") else null
                    Triple(conversation.name, args[0], kickReason)
                }
                // Error: necesita especificar canal
                else -> {
                    return error(context.getString(R.string.kick_error_no_channel))
                }
            }

            // Validar permisos de operador
            if (!PermissionValidator.hasOpPermission(service, channel)) {
                return error(context.getString(R.string.error_no_permission))
            }

            // Ejecutar comando usando sendRaw ya que no hay kickUser en IRCService
            val kickCommand = if (reason != null) {
                "KICK $channel $nick :$reason"
            } else {
                "KICK $channel $nick"
            }
            service.sendRawCommand(kickCommand)
            
            val successMsg = if (reason != null) {
                context.getString(R.string.kick_success_with_reason, nick, channel, reason)
            } else {
                context.getString(R.string.kick_success, nick, channel)
            }
            
            return success(successMsg)
            
        } catch (e: Exception) {
            return error(context.getString(R.string.kick_error_general, e.message ?: ""))
        }
    }
}
