package cd.software.flowchat.commands

import cd.software.flowchat.IRCService
import cd.software.flowchat.irc.conversation.ConversationManager
import cd.software.flowchat.model.Conversation

/**
 * Interfaz base para todos los comandos IRC.
 * Todos los comandos deben implementar esta interfaz para ser manejados por el sistema de comandos.
 */
interface IRCCommand {
    /**
     * Nombre principal del comando (sin la barra '/').
     */
    val name: String

    /**
     * Lista de alias alternativos para el comando.
     */
    val aliases: List<String>

    /**
     * Descripción corta del propósito del comando.
     */
    val description: String

    /**
     * Explicación de sintaxis y uso del comando.
     */
    val usage: String

    /**
     * Indica si el comando está disponible en canales.
     */
    val availableInChannel: Boolean

    /**
     * Indica si el comando está disponible en mensajes privados.
     */
    val availableInPrivate: Boolean

    /**
     * Indica si el comando requiere una conexión activa al servidor IRC.
     */
    val requiresConnection: Boolean

    /**
     * Ejecuta el comando.
     *
     * @param service El servicio IRC usado para realizar acciones en el servidor
     * @param conversation La conversación actual donde se ejecutó el comando
     * @param args Los argumentos proporcionados al comando
     * @param rawMessage El mensaje completo original
     * @return Resultado de la ejecución del comando
     */
    suspend fun execute(
        service: IRCService,
        conversation: Conversation,
        args: List<String>,
        rawMessage: String
    ): CommandResult
}
/**
 * Clase que representa el resultado de ejecutar un comando
 */
data class CommandResult(
    /**
     * Indica si el comando se ejecutó exitosamente
     */
    val success: Boolean,
    
    /**
     * Mensaje de éxito o error para mostrar al usuario
     */
    val message: String,
    
    /**
     * Indica si el mensaje debe agregarse a la conversación actual
     */
    val showInChat: Boolean = true,
    
    /**
     * Indica si el comando fue manejado localmente sin enviarlo al servidor
     */
    val handledLocally: Boolean = false
) 