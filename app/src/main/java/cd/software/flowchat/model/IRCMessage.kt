package cd.software.flowchat.model

data class IRCMessage(
    val sender: String,
    var content: String,
    val conversationType: ConversationType,
    val type: MessageType,
    val eventType: MessageEventType = MessageEventType.TEXT,
    val isOwnMessage: Boolean = false,
    val isMentioned: Boolean = false, // Nuevo campo
    val channelName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val additionalInfo: Map<String, String?> = emptyMap(),
    val eventColor: Long? = null,
    val eventColorType: EventColorType = EventColorType.TEXT,
    val isRead: Boolean = false
)
