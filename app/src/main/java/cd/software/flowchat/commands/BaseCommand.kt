package cd.software.flowchat.commands

import android.content.Context
import cd.software.flowchat.IRCService


/**
 * Clase base abstracta para simplificar la implementación de comandos IRC.
 * Proporciona implementaciones predeterminadas e implementa la interfaz IRCCommand.
 */
abstract class BaseCommand(protected val context: Context) : IRCCommand {

    /**
     * Crea un resultado exitoso
     */
    protected fun success(message: String, showInChat: Boolean = true, handledLocally: Boolean = false): CommandResult {
        return CommandResult(
            success = true,
            message = message,
            showInChat = showInChat,
            handledLocally = handledLocally
        )
    }

    /**
     * Crea un resultado fallido
     */
    protected fun error(message: String, showInChat: Boolean = true): CommandResult {
        return CommandResult(
            success = false,
            message = message,
            showInChat = showInChat,
            handledLocally = true
        )
    }

    /**
     * Valida que haya al menos un número específico de argumentos
     */
    protected fun validateMinArgs(args: List<String>, minArgs: Int, errorMessage: String): Boolean {
        return if (args.size < minArgs) {
            false
        } else {
            true
        }
    }

    /**
     * Función auxiliar para enviar un comando raw al servidor
     */
    protected suspend fun sendRawCommand(service: IRCService, command: String): Boolean {
        return try {
            service.bot?.sendRaw()?.rawLine(command)
            true
        } catch (e: Exception) {
            false
        }
    }
}