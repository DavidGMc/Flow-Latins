package cd.software.flowchat.irc.message

import android.content.Context
import cd.software.flowchat.model.ConversationType
import cd.software.flowchat.model.EventColorType
import cd.software.flowchat.model.IRCMessage
import cd.software.flowchat.model.MessageEventType
import cd.software.flowchat.model.MessageType
import cd.software.flowchat.preferences.ChatPreferences
import es.chat.R
import kotlinx.coroutines.flow.first
import org.pircbotx.hooks.events.ActionEvent
import org.pircbotx.hooks.events.MessageEvent
import org.pircbotx.hooks.events.NoticeEvent
import org.pircbotx.hooks.events.PrivateMessageEvent

/**
 * Procesa y formatea mensajes IRC. Responsable de convertir eventos IRC en objetos IRCMessage y
 * aplicar filtros.
 */
class IRCMessageHandler(
        private val chatPreferences: ChatPreferences,
        private val applicationContext: Context
) {

    /** Procesa un mensaje de canal. */
    suspend fun processChannelMessage(event: MessageEvent, currentNick: String): IRCMessage? {
        val username = event.user?.nick ?: return null
        val messageContent = event.message
        val channelName = event.channel.name

        // Verificar si el usuario está ignorado
        if (isUserIgnored(username, channelName)) {
            return null
        }

        val isMentioned = checkForMention(messageContent, currentNick)

        return IRCMessage(
                sender = username,
                content = messageContent,
                conversationType = ConversationType.CHANNEL,
                type = MessageType.TEXT,
                isOwnMessage = false,
                isMentioned = isMentioned,
                channelName = channelName
        )
    }

    /** Procesa un mensaje privado. */
    suspend fun processPrivateMessage(
            event: PrivateMessageEvent,
            currentNick: String
    ): IRCMessage? {
        val username = event.user?.nick ?: return null
        val messageContent = event.message

        // Verificar si el usuario está ignorado globalmente
        if (isUserIgnored(username)) {
            return null
        }

        // Verificar si los privados están bloqueados
        val blockPrivateMessages = chatPreferences.blockPrivateMessages.first()
        if (blockPrivateMessages) {
            return null // El caller debe enviar mensaje de bloqueo
        }

        // Verificar si el usuario está bloqueado
        if (isUserBlocked(username)) {
            return null // El caller debe enviar mensaje de bloqueo
        }

        val isMentioned = checkForMention(messageContent, currentNick)

        return IRCMessage(
                sender = username,
                content = messageContent,
                conversationType = ConversationType.PRIVATE_MESSAGE,
                type = MessageType.TEXT,
                isOwnMessage = false,
                isMentioned = isMentioned,
                channelName = username
        )
    }

    /** Procesa un notice. */
    suspend fun processNotice(event: NoticeEvent, currentNick: String? = null): IRCMessage? {
        // Si el usuario es null, es un notice del servidor
        if (event.user == null) {
            return IRCMessage(
                    sender = "Server",
                    content = event.message,
                    conversationType = ConversationType.SERVER_STATUS,
                    type = MessageType.NOTICE,
                    eventType = MessageEventType.SERVER_NOTICE,
                    channelName = "Status",
                    eventColorType = EventColorType.NOTICE
            )
        }

        val sender = event.user!!.nick
        val channel = event.channel

        return if (channel != null) {
            // Notice en un canal
            IRCMessage(
                    sender = sender,
                    content = event.message,
                    conversationType = ConversationType.CHANNEL,
                    type = MessageType.NOTICE,
                    isOwnMessage = false,
                    channelName = channel.name,
                    eventColorType = EventColorType.NOTICE
            )
        } else {
            // Notice privado
            IRCMessage(
                    sender = sender,
                    content = event.message,
                    conversationType = ConversationType.PRIVATE_MESSAGE,
                    type = MessageType.NOTICE,
                    isOwnMessage = false,
                    channelName = sender,
                    eventColorType = EventColorType.NOTICE
            )
        }
    }

    /** Procesa una acción (/me). */
    suspend fun processAction(event: ActionEvent): IRCMessage? {
        val username = event.user?.nick ?: return null
        val actionContent = event.message

        // Verificar si el usuario está ignorado
        if (event.channel != null) {
            val channelName = event.channel!!.name
            if (isUserIgnored(username, channelName)) {
                return null
            }

            return IRCMessage(
                    sender = username,
                    content = "* $username $actionContent",
                    conversationType = ConversationType.CHANNEL,
                    type = MessageType.ACTION,
                    isOwnMessage = false,
                    channelName = channelName,
                    eventColorType = EventColorType.ACTION
            )
        } else {
            // Acción en privado
            if (isUserIgnored(username)) {
                return null
            }

            return IRCMessage(
                    sender = username,
                    content = "* $username $actionContent",
                    conversationType = ConversationType.PRIVATE_MESSAGE,
                    type = MessageType.ACTION,
                    isOwnMessage = false,
                    channelName = username,
                    eventColorType = EventColorType.ACTION
            )
        }
    }

    /** Crea un mensaje de evento IRC. */
    fun createEventMessage(
            eventType: MessageEventType,
            channel: String,
            user: String,
            additionalInfo: Map<String, String?> = emptyMap()
    ): IRCMessage {
        val colorType = getEventColorType(eventType)
        val content = formatEventContent(eventType, user, additionalInfo)

        return IRCMessage(
                sender = "",
                content = content,
                conversationType = ConversationType.CHANNEL,
                type = MessageType.TEXT,
                eventType = eventType,
                channelName = channel,
                additionalInfo = additionalInfo,
                eventColorType = colorType,
                timestamp = System.currentTimeMillis()
        )
    }

    /** Crea un mensaje del sistema. */
    fun createSystemMessage(content: String, channelName: String = "Status"): IRCMessage {
        return IRCMessage(
                sender = applicationContext.getString(R.string.system_sender),
                content = content,
                conversationType = ConversationType.SERVER_STATUS,
                type = MessageType.NOTICE,
                eventType = MessageEventType.SERVER_NOTICE,
                channelName = channelName,
                eventColorType = EventColorType.SYSTEM_SYNC
        )
    }

    /** Crea un mensaje de sincronización del sistema. */
    fun createSystemSyncMessage(message: String): IRCMessage {
        return IRCMessage(
                sender = applicationContext.getString(R.string.system_sender),
                content = applicationContext.getString(R.string.sync_message_prefix, message),
                conversationType = ConversationType.SERVER_STATUS,
                type = MessageType.NOTICE,
                eventType = MessageEventType.SERVER_NOTICE,
                channelName = "Status",
                eventColorType = EventColorType.SYSTEM_SYNC
        )
    }

    /** Verifica si un mensaje menciona al usuario. */
    fun checkForMention(message: String, username: String): Boolean {
        return message.contains("@$username") || message.contains(username)
    }

    /** Verifica si un usuario está ignorado. */
    private suspend fun isUserIgnored(username: String, channelName: String? = null): Boolean {
        // Verificar ignorado globalmente
        val isGloballyIgnored = chatPreferences.ignoredUsers.first().contains(username)
        if (isGloballyIgnored) {
            return true
        }

        // Verificar ignorado en canal específico
        if (channelName != null) {
            val ignoredUsersInChannel = chatPreferences.ignoredUsersInChannel(channelName).first()
            if (ignoredUsersInChannel.contains(username)) {
                return true
            }
        }

        return false
    }

    /** Verifica si un usuario está bloqueado. */
    private suspend fun isUserBlocked(username: String): Boolean {
        return chatPreferences.isUserBlocked(username).first()
    }

    /** Obtiene el tipo de color para un evento. */
    private fun getEventColorType(eventType: MessageEventType): EventColorType {
        return when (eventType) {
            MessageEventType.NICK_CHANGE -> EventColorType.NICK_CHANGE
            MessageEventType.USER_JOIN -> EventColorType.USER_JOIN
            MessageEventType.USER_PART -> EventColorType.USER_PART
            MessageEventType.USER_QUIT -> EventColorType.USER_QUIT
            MessageEventType.USER_KICK -> EventColorType.USER_KICK
            MessageEventType.USER_BAN -> EventColorType.USER_BAN
            MessageEventType.USER_OP -> EventColorType.USER_OP
            MessageEventType.USER_DEOP -> EventColorType.USER_DEOP
            MessageEventType.USER_VOICE -> EventColorType.USER_VOICE
            MessageEventType.USER_DEVOICE -> EventColorType.USER_DEVOICE
            MessageEventType.USER_HALFOP -> EventColorType.USER_HALFOP
            MessageEventType.USER_DEHALFOP -> EventColorType.USER_DEHALFOP
            MessageEventType.SERVER_NOTICE -> EventColorType.SERVER_NOTICE
            MessageEventType.NOTICE -> EventColorType.SERVER_NOTICE
            else -> EventColorType.SYSTEM_GENERAL
        }
    }

    /** Formatea el contenido de un evento. */
    private fun formatEventContent(
            eventType: MessageEventType,
            user: String,
            additionalInfo: Map<String, String?>
    ): String {
        return when (eventType) {
            MessageEventType.NICK_CHANGE -> {
                val oldNick = additionalInfo["oldNick"] ?: ""
                val channelName = additionalInfo["channelName"] ?: ""
                applicationContext.getString(R.string.event_nick_change, oldNick, user, channelName)
            }
            MessageEventType.USER_JOIN -> {
                applicationContext.getString(R.string.user_joined, user)
            }
            MessageEventType.USER_PART -> {
                applicationContext.getString(R.string.user_left, user)
            }
            MessageEventType.USER_QUIT -> {
                applicationContext.getString(R.string.user_quit, user)
            }
            MessageEventType.USER_KICK -> {
                val kicked = additionalInfo["kicked"] ?: user
                val moderator =
                        additionalInfo["moderator"]
                                ?: applicationContext.getString(R.string.unknown_sender)
                applicationContext.getString(R.string.user_kicked, kicked, moderator)
            }
            MessageEventType.USER_BAN -> {
                val reason = additionalInfo["reason"] ?: ""
                applicationContext.getString(R.string.user_ban, user, reason)
            }
            MessageEventType.USER_UNBAN -> {
                applicationContext.getString(R.string.user_unban, user)
            }
            MessageEventType.USER_OP -> {
                applicationContext.getString(R.string.user_op, user)
            }
            MessageEventType.USER_DEOP -> {
                applicationContext.getString(R.string.user_deop, user)
            }
            MessageEventType.USER_VOICE -> {
                applicationContext.getString(R.string.user_voice, user)
            }
            MessageEventType.USER_DEVOICE -> {
                applicationContext.getString(R.string.user_devoice, user)
            }
            MessageEventType.USER_HALFOP -> {
                applicationContext.getString(R.string.user_halfop, user)
            }
            MessageEventType.USER_DEHALFOP -> {
                applicationContext.getString(R.string.user_dehalfop, user)
            }
            MessageEventType.USER_DISCONNECTED -> {
                applicationContext.getString(R.string.user_disconnected, user)
            }
            MessageEventType.SERVER_NOTICE -> user
            else -> user
        }
    }
}
