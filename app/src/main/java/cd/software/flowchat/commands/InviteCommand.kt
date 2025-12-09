package cd.software.flowchat.commands

import android.content.Context
import cd.software.flowchat.IRCService
import cd.software.flowchat.model.Conversation
import cd.software.flowchat.model.ConversationType
import es.chat.R

/**
 * Comando para invitar a un usuario a un canal IRC.
 * Uso: /invite <nick> [#canal]
 * 
 * Puede requerir permisos de operador dependiendo de la configuración del canal.
 * Algunos canales permiten que cualquier usuario invite, otros solo operadores.
 */
class InviteCommand(context: Context) : BaseCommand(context) {
    override val name = "invite"
    override val aliases = listOf("inv")
    override val description = context.getString(R.string.invite_command_description)
    override val usage = context.getString(R.string.invite_command_usage)
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
        if (!validateMinArgs(args, 1, context.getString(R.string.invite_error_no_nick))) {
            return error(context.getString(R.string.invite_error_usage, usage))
        }

        try {
            // Determinar nickname y canal
            val (nick, channel) = when {
                // Si hay dos argumentos: /invite nick #canal
                args.size > 1 -> {
                    // Validar que el segundo argumento sea un canal
                    if (!args[1].startsWith("#")) {
                        return error(context.getString(R.string.invite_error_invalid_channel))
                    }
                    args[0] to args[1]
                }
                // Si estamos en un canal: /invite nick
                conversation.type == ConversationType.CHANNEL -> {
                    args[0] to conversation.name
                }
                // Error: necesita especificar canal
                else -> {
                    return error(context.getString(R.string.invite_error_no_channel))
                }
            }

            // Nota: No validamos permisos aquí porque algunos canales permiten
            // que cualquier usuario invite. Si el usuario no tiene permisos,
            // el servidor rechazará el comando.
            
            // Ejecutar comando
            service.inviteUser(nick, channel)
            return success(context.getString(R.string.invite_success, nick, channel))
            
        } catch (e: Exception) {
            return error(context.getString(R.string.invite_error_general, e.message ?: ""))
        }
    }
}
