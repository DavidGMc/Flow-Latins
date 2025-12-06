package cd.software.flowchat.model

import java.util.UUID

data class Conversation(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: ConversationType,
    val origin: ConversationType = ConversationType.CHANNEL,
    val messages: MutableList<IRCMessage> = mutableListOf(),
    val lastReadTimestamp: Long = 0L,
    val isActive: Boolean = true
){

    fun markAsRead(): Conversation {
        return this.copy(messages = messages.map { it.copy(isRead = true) }.toMutableList())
    }

}
