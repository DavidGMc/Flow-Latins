package cd.software.flowchat.irc.events

import android.content.Context
import android.util.Log
import cd.software.flowchat.irc.conversation.ConversationManager
import cd.software.flowchat.irc.message.IRCMessageHandler
import cd.software.flowchat.model.Channel
import cd.software.flowchat.model.ConversationType
import cd.software.flowchat.model.EventColorType
import cd.software.flowchat.model.IRCMessage
import cd.software.flowchat.model.MessageEventType
import cd.software.flowchat.model.MessageType
import cd.software.flowchat.preferences.ChatPreferences
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.pircbotx.PircBotX
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.*

/**
 * Maneja todos los eventos IRC de PircBotX. Responsable de procesar eventos y delegarlos a los
 * handlers apropiados.
 */
class IRCEventHandler(
        private val messageHandler: IRCMessageHandler,
        private val conversationManager: ConversationManager,
        private val chatPreferences: ChatPreferences,
        private val applicationContext: Context,
        private val serviceScope: CoroutineScope,
        private val onConnectionStateChange: (ConnectionStateUpdate) -> Unit,
        private val getBotNick: () -> String?,
        private val sendRawLine: (String) -> Unit
) : ListenerAdapter() {

    // ========== Eventos de Conexión ==========

    override fun onConnect(event: ConnectEvent) {
        val serverAddress =
                try {
                    getBotNick() ?: "servidor IRC"
                } catch (e: Exception) {
                    "servidor IRC"
                }

        onConnectionStateChange(ConnectionStateUpdate.Connected)

        val syncMessage = "Sincronización activa: Conectado a $serverAddress"
        conversationManager.getOrCreateStatusConversation()
        conversationManager.addMessage(messageHandler.createSystemSyncMessage(syncMessage))

        Log.d("IRCSync", syncMessage)
    }

    override fun onDisconnect(event: DisconnectEvent) {
        onConnectionStateChange(ConnectionStateUpdate.Disconnected)

        val syncMessage = "Sincronización interrumpida: Desconectado del servidor"
        conversationManager.addMessage(messageHandler.createSystemSyncMessage(syncMessage))

        Log.d("IRCSync", syncMessage)
    }

    // ========== Eventos de Mensajes ==========

    override fun onMessage(event: MessageEvent) {
        serviceScope.launch {
            val currentNick = getBotNick() ?: return@launch
            val message = messageHandler.processChannelMessage(event, currentNick)

            if (message != null) {
                conversationManager.addMessage(message)
            }
        }
    }

    override fun onPrivateMessage(event: PrivateMessageEvent) {
        serviceScope.launch {
            val username = event.user?.nick ?: return@launch
            val currentNick = getBotNick() ?: return@launch

            // Verificar bloqueos
            val blockPrivateMessages = chatPreferences.blockPrivateMessages.first()
            if (blockPrivateMessages) {
                sendRawLine("PRIVMSG $username :Mis privados están bloqueados")
                return@launch
            }

            val isUserBlocked = chatPreferences.isUserBlocked(username).first()
            if (isUserBlocked) {
                sendRawLine("PRIVMSG $username :Has sido bloqueado")
                return@launch
            }

            val message = messageHandler.processPrivateMessage(event, currentNick)
            if (message != null) {
                conversationManager.startPrivateConversation(username)
                conversationManager.addMessage(message)
            }
        }
    }

    override fun onNotice(event: NoticeEvent) {
        serviceScope.launch {
            val currentNick = getBotNick()
            val message = messageHandler.processNotice(event, currentNick)

            if (message != null) {
                // Si es un notice de usuario en un canal
                if (event.user != null && event.channel == null) {
                    val sender = event.user!!.nick
                    val userChannels = event.user?.channels?.map { it.name } ?: emptyList()
                    val ourChannels =
                            conversationManager.conversations.value
                                    .filter { it.type == ConversationType.CHANNEL }
                                    .map { it.name }

                    val commonChannels = userChannels.filter { ourChannels.contains(it) }

                    if (commonChannels.isNotEmpty()) {
                        // Mostrar en todos los canales compartidos
                        commonChannels.forEach { sharedChannel ->
                            conversationManager.addMessage(
                                    message.copy(
                                            content = "[← Notice] ${event.message}",
                                            channelName = sharedChannel
                                    )
                            )
                        }
                    } else {
                        // Usar conversación privada
                        conversationManager.startPrivateConversation(sender)
                        conversationManager.addMessage(message)
                    }
                } else {
                    conversationManager.getOrCreateStatusConversation()
                    conversationManager.addMessage(message)
                }
            }
        }
    }

    override fun onAction(event: ActionEvent) {
        serviceScope.launch {
            val message = messageHandler.processAction(event)
            if (message != null) {
                if (event.channel == null) {
                    conversationManager.startPrivateConversation(event.user?.nick ?: return@launch)
                }
                conversationManager.addMessage(message)
            }
        }
    }

    // ========== Eventos de Usuarios ==========

    override fun onJoin(event: JoinEvent) {
        conversationManager.handleAutoJoinChannel(event.channel.name)

        handleIRCEvent(MessageEventType.USER_JOIN, event.channel.name, event.user!!.nick)
    }

    override fun onPart(event: PartEvent) {
        if (event.user.nick == getBotNick()) {
            conversationManager.confirmChannelRemoval(event.channel.name)
        }

        handleIRCEvent(MessageEventType.USER_PART, event.channel.name, event.user.nick)
    }

    override fun onQuit(event: QuitEvent) {
        // Notificar en todos los canales donde estaba el usuario
        event.user.channels.forEach { channel ->
            handleIRCEvent(MessageEventType.USER_QUIT, channel.name, event.user.nick)
        }

        // Manejar desconexión en conversación privada
        val privateConversation =
                conversationManager.conversations.value.find {
                    it.type == ConversationType.PRIVATE_MESSAGE && it.name == event.user.nick
                }

        if (privateConversation != null) {
            conversationManager.addMessage(
                    IRCMessage(
                            sender = "System",
                            content = "${event.user.nick} has disconnected.",
                            conversationType = ConversationType.PRIVATE_MESSAGE,
                            type = MessageType.TEXT,
                            eventType = MessageEventType.USER_DISCONNECTED,
                            channelName = event.user.nick
                    )
            )
        }
    }

    override fun onKick(event: KickEvent) {
        val additionalInfo =
                mapOf("kicked" to event.recipient!!.nick, "moderator" to event.user?.nick)

        handleIRCEvent(
                MessageEventType.USER_KICK,
                event.channel.name,
                event.recipient!!.nick,
                additionalInfo
        )
    }

    override fun onNickChange(event: NickChangeEvent) {
        val channelsForUser = event.user!!.channels

        if (channelsForUser.isNotEmpty()) {
            channelsForUser.forEach { channel ->
                handleIRCEvent(
                        MessageEventType.NICK_CHANGE,
                        channel.name,
                        event.newNick,
                        mapOf("oldNick" to event.oldNick, "channelName" to channel.name)
                )
            }
        } else {
            handleIRCEvent(
                    MessageEventType.NICK_CHANGE,
                    "Global",
                    event.newNick,
                    mapOf("oldNick" to event.oldNick)
            )
        }
    }

    // ========== Eventos de Canal ==========

    override fun onMode(event: ModeEvent) {
        val additionalInfo = mapOf("mode" to event.mode, "setter" to event.user!!.nick)

        val eventType =
                when {
                    event.mode.contains("+o") -> MessageEventType.USER_OP
                    event.mode.contains("-o") -> MessageEventType.USER_DEOP
                    event.mode.contains("+v") -> MessageEventType.USER_VOICE
                    event.mode.contains("-v") -> MessageEventType.USER_DEVOICE
                    event.mode.contains("+h") -> MessageEventType.USER_HALFOP
                    event.mode.contains("-h") -> MessageEventType.USER_DEHALFOP
                    event.mode.contains("+b") -> MessageEventType.USER_BAN
                    event.mode.contains("-b") -> MessageEventType.USER_UNBAN
                    else -> MessageEventType.MODE_CHANGE
                }

        handleIRCEvent(eventType, event.channel.name, event.mode, additionalInfo)
    }

    override fun onTopic(event: TopicEvent) {
        serviceScope.launch {
            val channel = event.channel.name
            val user = event.user?.nick ?: "Server"
            val topic = event.topic
            val oldTopic = event.oldTopic ?: "No topic"
            val changed = event.isChanged

            val messageContent =
                    if (changed) {
                        "Topic por $user: \"$topic\""
                    } else {
                        "Tema : \"$topic\""
                    }

            val topicEvent =
                    IRCMessage(
                            sender = user,
                            content = messageContent,
                            conversationType = ConversationType.CHANNEL,
                            type = MessageType.TEXT,
                            eventType = MessageEventType.TOPIC_CHANGE,
                            channelName = channel,
                            additionalInfo =
                                    mapOf(
                                            "oldTopic" to oldTopic,
                                            "newTopic" to topic,
                                            "date" to event.date.toString()
                                    ),
                            eventColorType = EventColorType.TOPIC_CHANGE
                    )

            conversationManager.addMessage(topicEvent)
        }
    }

    override fun onUserList(event: UserListEvent) {
        serviceScope.launch {
            val channelName = event.channel.name
            val users = event.users.map { it.nick }

            // Solo mostrar si hay 5 usuarios o menos
            if (users.size <= 5) {
                conversationManager.addMessage(
                        IRCMessage(
                                sender = "System",
                                content = "Usuarios en $channelName: ${users.joinToString(", ")}",
                                conversationType = ConversationType.CHANNEL,
                                type = MessageType.TEXT,
                                eventType = MessageEventType.USER_LIST,
                                channelName = channelName,
                                eventColorType = EventColorType.SYSTEM_GENERAL
                        )
                )
            }
        }
    }

    override fun onChannelInfo(event: ChannelInfoEvent) {
        val channels =
                event.list.map { channelEntry ->
                    Channel(
                            name = channelEntry.name,
                            topic = channelEntry.topic ?: "No topic",
                            userCount = channelEntry.users
                    )
                }

        val syncMessage = "Sincronizados ${channels.size} canales del servidor"
        conversationManager.addMessage(messageHandler.createSystemSyncMessage(syncMessage))
        conversationManager.updateAvailableChannels(channels)

        Log.d("IRCSync", syncMessage)
    }

    // ========== Eventos del Servidor ==========

    override fun onServerResponse(event: ServerResponseEvent) {
        val code = event.code
        val message = event.rawLine

        if ((code in 1..99) || (code in 200..399) || (code in 400..599)) {
            if (isRelevantServerMessage(message)) {
                conversationManager.getOrCreateStatusConversation()
                conversationManager.addMessage(
                        IRCMessage(
                                sender = "Server",
                                content = message,
                                conversationType = ConversationType.SERVER_STATUS,
                                type = MessageType.NOTICE,
                                eventType = MessageEventType.SERVER_NOTICE,
                                channelName = "Status"
                        )
                )
            }
        }
    }

    override fun onWhois(event: WhoisEvent) {
        serviceScope.launch {
            val nick = event.nick
            val realName =
                    when {
                        event.realname.isNullOrBlank() -> "No disponible"
                        else -> event.realname
                    }
            val login = event.login
            val host = event.hostname
            val server = event.server ?: "Desconocido"
            val serverInfo = event.serverInfo ?: "Sin información del servidor"
            val idle = event.idleSeconds
            val signOn = DateFormat.getDateTimeInstance().format(Date(event.signOnTime))

            val channels = event.channels
            val opChannels = channels.filter { it.startsWith("@") }.map { it.removePrefix("@") }
            val normalChannels = channels.filter { !it.startsWith("@") }

            val channelsText = buildString {
                if (opChannels.isNotEmpty()) {
                    appendLine("🔧 Operador en: ${opChannels.joinToString(", ")}")
                }
                if (normalChannels.isNotEmpty()) {
                    appendLine("👥 Canales: ${normalChannels.joinToString(", ")}")
                }
                if (isEmpty()) {
                    appendLine("No está en ningún canal visible.")
                }
            }

            val whoisInfo =
                    """
                |📄 WHOIS para $nick
                |👤 Nombre real: $realName
                |🔐 Login: $login
                |🌐 Host: $host
                |🛰️ Servidor: $server
                |📝 Info del servidor: $serverInfo
                |⏱️ Inactivo: ${idle}s
                |📆 Conectado desde: $signOn
                |$channelsText
            """.trimMargin()

            conversationManager.addMessage(
                    IRCMessage(
                            sender = "System",
                            content = whoisInfo,
                            conversationType = ConversationType.SERVER_STATUS,
                            type = MessageType.TEXT,
                            eventType = MessageEventType.WHOIS_RESPONSE,
                            channelName = "Status",
                            eventColorType = EventColorType.SYSTEM_INFO
                    )
            )
        }
    }

    override fun onInvite(event: InviteEvent) {
        serviceScope.launch {
            val inviter = event.user?.nick ?: "Alguien"
            val channel = event.channel

            conversationManager.addMessage(
                    IRCMessage(
                            sender = "System",
                            content = "$inviter te ha invitado al canal $channel",
                            conversationType = ConversationType.SERVER_STATUS,
                            type = MessageType.TEXT,
                            eventType = MessageEventType.INVITE,
                            channelName = "Status",
                            eventColorType = EventColorType.INVITE
                    )
            )
        }
    }

    // ========== Eventos CTCP ==========

    override fun onVersion(event: VersionEvent) {
        serviceScope.launch {
            val sender = event.user.nick
            conversationManager.addMessage(
                    IRCMessage(
                            sender = sender,
                            content = "Solicitud CTCP VERSION recibida",
                            conversationType = ConversationType.SERVER_STATUS,
                            type = MessageType.TEXT,
                            eventType = MessageEventType.CTCP_REQUEST,
                            channelName = "Status",
                            eventColorType = EventColorType.SYSTEM_INFO
                    )
            )
        }
    }

    override fun onFinger(event: FingerEvent) {
        serviceScope.launch {
            val sender = event.user!!.nick
            conversationManager.addMessage(
                    IRCMessage(
                            sender = sender,
                            content = "Solicitud CTCP FINGER recibida",
                            conversationType = ConversationType.SERVER_STATUS,
                            type = MessageType.TEXT,
                            eventType = MessageEventType.CTCP_REQUEST,
                            channelName = "Status",
                            eventColorType = EventColorType.SYSTEM_INFO
                    )
            )
        }
    }

    override fun onTime(event: TimeEvent) {
        serviceScope.launch {
            val sender = event.user?.nick ?: "Unknown"
            conversationManager.addMessage(
                    IRCMessage(
                            sender = sender,
                            content = "Solicitud CTCP TIME recibida",
                            conversationType = ConversationType.SERVER_STATUS,
                            type = MessageType.TEXT,
                            eventType = MessageEventType.CTCP_REQUEST,
                            channelName = "Status",
                            eventColorType = EventColorType.SYSTEM_INFO
                    )
            )
        }
    }

    override fun onServerPing(event: ServerPingEvent) {
        serviceScope.launch { Log.d("IRCService", "Server ping received and handled") }
    }

    override fun onUserMode(event: UserModeEvent) {
        serviceScope.launch {
            val mode = event.mode
            conversationManager.addMessage(
                    IRCMessage(
                            sender = "System",
                            content = "Tu modo de usuario ha cambiado: $mode",
                            conversationType = ConversationType.SERVER_STATUS,
                            type = MessageType.TEXT,
                            eventType = MessageEventType.USER_MODE_CHANGE,
                            channelName = "Status",
                            eventColorType = EventColorType.SYSTEM_INFO
                    )
            )
        }
    }

    override fun onUnknown(event: UnknownEvent) {
        val line = event.line

        if (isRelevantUnknownCommand(line)) {
            serviceScope.launch {
                conversationManager.addMessage(
                        IRCMessage(
                                sender = "Server",
                                content = "Comando desconocido: $line",
                                conversationType = ConversationType.SERVER_STATUS,
                                type = MessageType.NOTICE,
                                eventType = MessageEventType.UNKNOWN_COMMAND,
                                channelName = "Status",
                                eventColorType = EventColorType.SERVER_NOTICE
                        )
                )
            }
        }
    }

    // ========== Métodos Auxiliares ==========

    private fun handleIRCEvent(
            eventType: MessageEventType,
            channel: String,
            user: String,
            additionalInfo: Map<String, String?> = emptyMap()
    ) {
        serviceScope.launch {
            if (!shouldShowEvent(eventType)) return@launch

            val eventMessage =
                    messageHandler.createEventMessage(eventType, channel, user, additionalInfo)

            conversationManager.addMessage(eventMessage)
        }
    }

    private suspend fun shouldShowEvent(eventType: MessageEventType): Boolean {
        return when (eventType) {
            MessageEventType.USER_JOIN -> chatPreferences.showJoinEvents.first()
            MessageEventType.USER_QUIT -> chatPreferences.showQuitEvents.first()
            MessageEventType.USER_PART -> chatPreferences.showPartEvents.first()
            MessageEventType.USER_BAN -> chatPreferences.showBanEvents.first()
            MessageEventType.USER_KICK -> true
            else -> true
        }
    }

    private fun isRelevantServerMessage(message: String): Boolean {
        val relevantPatterns =
                listOf(
                        ".*Connected to.*",
                        ".*Connection established.*",
                        ".*Successfully connected.*",
                        ".*Logged in.*",
                        ".*registered.*",
                        ".*identified.*",
                        ".*Authentication successful.*",
                        ".*rules.*",
                        ".*RULES.*",
                        ".*Guidelines.*",
                        ".*MOTD.*",
                        ".*Error:.*",
                        ".*Failed.*",
                        ".*rejected.*",
                        ".*Welcome.*",
                        ".*welcome.*",
                        ".*Server info.*",
                        ".*network policy.*",
                        ".*Network Policy.*"
                )

        return relevantPatterns.any { pattern ->
            message.matches(Regex(pattern, RegexOption.IGNORE_CASE))
        }
    }

    private fun isRelevantUnknownCommand(line: String): Boolean {
        val relevantPrefixes =
                listOf(
                        "ERROR",
                        "AUTH",
                        "CAP",
                        "AUTHENTICATE",
                        "ACCOUNT",
                        "CHGHOST",
                        "AWAY",
                        "WATCH"
                )

        return relevantPrefixes.any { prefix -> line.startsWith(prefix, ignoreCase = true) }
    }
}

/** Representa una actualización del estado de conexión. */
sealed class ConnectionStateUpdate {
    object Connected : ConnectionStateUpdate()
    object Connecting : ConnectionStateUpdate()
    object Disconnected : ConnectionStateUpdate()
    data class Error(val message: String) : ConnectionStateUpdate()
}
