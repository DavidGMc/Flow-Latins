package cd.software.flowchat.commands

import android.util.Log
import cd.software.flowchat.IRCService
import cd.software.flowchat.irc.conversation.ConversationManager
import cd.software.flowchat.model.Conversation
import cd.software.flowchat.model.ConversationType
import cd.software.flowchat.model.IRCMessage
import cd.software.flowchat.model.MessageType

/**
 * Gestiona el registro y ejecución de comandos IRC.
 */
class CommandManager {
    private val commands = mutableMapOf<String, IRCCommand>()
    private val TAG = "CommandManager"

    /**
     * Registra un comando en el sistema.
     * También registra sus alias como entradas adicionales que apuntan al mismo comando.
     */
    fun registerCommand(command: IRCCommand) {
        // Registra el comando por su nombre principal
        commands[command.name.lowercase()] = command

        // Registra el comando por sus alias
        command.aliases.forEach { alias ->
            commands[alias.lowercase()] = command
        }

        Log.d(TAG, "Comando registrado: /${command.name} (${command.aliases.joinToString(", ") { "/$it" }})")
    }

    /**
     * Registra múltiples comandos a la vez
     */
    fun registerCommands(commandList: List<IRCCommand>) {
        commandList.forEach { registerCommand(it) }
    }

    /**
     * Procesa un mensaje y determina si es un comando que debe ejecutarse.
     * Si el mensaje comienza con '/', se tratará como un comando.
     *
     * @param message El texto del mensaje ingresado por el usuario
     * @param service El servicio IRC para ejecutar acciones
     * @param conversation La conversación actual donde se ingresó el mensaje
     * @return Un par con:
     *         - Boolean: true si el mensaje fue procesado como comando, false si debe tratarse como mensaje normal
     *         - IRCMessage?: el mensaje para mostrar en la conversación, o null si no se debe mostrar nada
     */
    suspend fun processMessage(
        message: String,
        service: IRCService,
        conversation: Conversation
    ): Pair<Boolean, IRCMessage?> {
        // Si no comienza con /, no es un comando
        if (!message.startsWith("/")) {
            return Pair(false, null)
        }


        // Quitar la barra inicial y dividir el comando y sus argumentos
        val parts = message.substring(1).split("\\s+".toRegex(), limit = 2)
        val commandName = parts[0].lowercase()

        // Si el comando es solo una barra, no hacer nada
        if (commandName.isEmpty()) {
            val errorMessage = IRCMessage(
                sender = "Sistema",
                content = "Falta el comando a procesar. No puede estar vacío.",
                conversationType = conversation.type,
                type = MessageType.NOTICE,
                isOwnMessage = false,
                channelName = conversation.name
            )
            return Pair(true, errorMessage)
        }

        // Obtener argumentos (si existen)
        val args = if (parts.size > 1 && parts[1].isNotEmpty()) {
            parts[1].split("\\s+".toRegex())
        } else {
            emptyList()
        }

        val command = commands[commandName]

        // Modificación: Si el comando no existe, intentar enviarlo como raw
        if (command == null) {
            // Verificar si el usuario quiere enviar un comando raw estándar IRC
            if (isStandardIRCCommand(commandName)) {
                try {
                    // Contruir el comando completo
                    val rawCommandString = commandName.uppercase() + if (args.isNotEmpty()) " " + args.joinToString(" ") else ""
                    service.executeServerCommand(rawCommandString)

                    val successMessage = IRCMessage(
                        sender = "Sistema",
                        content = "Comando IRC enviado: $rawCommandString",
                        conversationType = conversation.type,
                        type = MessageType.TEXT,
                        isOwnMessage = false,
                        channelName = conversation.name
                    )
                    return Pair(true, successMessage)
                } catch (e: Exception) {
                    val errorMessage = IRCMessage(
                        sender = "Sistema",
                        content = "Error al enviar comando: ${e.message ?: "Error desconocido"}",
                        conversationType = conversation.type,
                        type = MessageType.TEXT,
                        isOwnMessage = false,
                        channelName = conversation.name
                    )
                    return Pair(true, errorMessage)
                }
            } else {
                val errorMessage = IRCMessage(
                    sender = "Sistema",
                    content = "Comando desconocido: /$commandName. Usa /help para ver los comandos disponibles.",
                    conversationType = conversation.type,
                    type = MessageType.TEXT,
                    isOwnMessage = false,
                    channelName = conversation.name
                )
                return Pair(true, errorMessage)
            }
        }


        // Verificar si el comando es válido para el tipo de conversación actual
        val isValidForConversation = when (conversation.type) {
            ConversationType.CHANNEL -> command.availableInChannel
            ConversationType.PRIVATE_MESSAGE -> command.availableInPrivate
            else -> true
        }

        if (!isValidForConversation) {
            val errorMessage = IRCMessage(
                sender = "Sistema",
                content = "El comando /${command.name} no está disponible en ${conversation.type}.",
                conversationType = conversation.type,
                type = MessageType.TEXT,
                isOwnMessage = false,
                channelName = conversation.name
            )
            return Pair(true, errorMessage)
        }

        // Verificar si el comando requiere conexión
        if (command.requiresConnection && !service.isConnected()) {
            val errorMessage = IRCMessage(
                sender = "Sistema",
                content = "No estás conectado a un servidor IRC. Debes conectarte primero para usar este comando.",
                conversationType = conversation.type,
                type = MessageType.TEXT,
                isOwnMessage = false,
                channelName = conversation.name
            )
            return Pair(true, errorMessage)
        }

        // Ejecutar el comando
        try {
            val result = command.execute(service, conversation, args, message)

            // Si el comando se maneja localmente, no enviarlo al servidor
            if (result.handledLocally) {
                Log.d(TAG, "Comando manejado localmente: $commandName")
            }

            // Si se debe mostrar un mensaje en el chat
            return if (result.showInChat) {
                val resultMessage = IRCMessage(
                    sender = "Sistema Flowirc",
                    content = result.message,
                    conversationType = conversation.type,
                    type = MessageType.TEXT,
                    isOwnMessage = false,
                    channelName = conversation.name
                )
                Pair(true, resultMessage)
            } else {
                Pair(true, null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al ejecutar comando $commandName", e)
            val errorMessage = IRCMessage(
                sender = "Sistema",
                content = "Error al ejecutar /${command.name}: ${e.message ?: "Error desconocido"}",
                conversationType = conversation.type,
                type = MessageType.TEXT,
                isOwnMessage = false,
                channelName = conversation.name
            )
            return Pair(true, errorMessage)
        }
    }

    /**
     * Obtiene todos los comandos registrados
     */
    fun getAllCommands(): List<IRCCommand> {
        return commands.values.distinct()
    }

    /**
     * Obtiene un comando específico por su nombre
     */
    fun getCommand(name: String): IRCCommand? {
        return commands[name.lowercase()]
    }
    // Función auxiliar para determinar si un comando es un comando IRC estándar
    private fun isStandardIRCCommand(command: String): Boolean {
        val standardCommands = setOf(
            "join", "part", "quit", "nick", "user", "mode", "topic", "invite",
            "kick", "privmsg", "notice", "who", "whois", "whowas", "away",
            "oper", "wallops", "userhost", "ison", "service", "names",
            "list", "stats", "links", "time", "connect", "trace", "admin",
            "info", "squery", "pong", "cap", "authenticate", "ping","verification","quote",
            "whoischan","whoisuser","whoisserver","whoisidle","whoischannels","whoisoperator",
            "whoisnon","sajoin", "rehash","kline","zline","gline","invite","wallops","globops",
            "chatops","locops","adchat","nachat","kill","gzline","restart","die","sethost",
            "setident","squit","chgname","chghost","sapart","samode","opermotd","operhost",
            "close","umode2","umode3","umode4","umode5","umode6","umode7","umode8","umode9","mode",
            "chan","msg chan ", "msg nick identify", "ison","motd","rules","topic","users","lusers","map",
            "quit","ping","version","admin","userhost","helphop","knock","setname","vhost","time",
            "botmod", "identify", "userip","stats","module","help"
        )
        return standardCommands.contains(command.lowercase())
    }
}