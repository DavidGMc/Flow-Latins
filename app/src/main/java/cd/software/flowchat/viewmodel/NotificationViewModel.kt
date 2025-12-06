package cd.software.flowchat.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cd.software.flowchat.NotificationService
import cd.software.flowchat.model.ConnectionState
import cd.software.flowchat.model.ConversationType
import cd.software.flowchat.model.IRCMessage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val context: Context,
    private val chatViewModel: IRCChatViewModel,
    private val connectionViewModel: IRCConnectionViewModel
) : ViewModel() {

    private val notificationService = NotificationService(context)
    private var isAppInForeground = true

    init {
        observeMessages()
    }

    fun setAppInForeground(inForeground: Boolean) {
        isAppInForeground = inForeground
    }

    private fun isMentioned(message: IRCMessage, nickname: String): Boolean {
        val mentionPatterns = listOf(
            "@$nickname",
            "$nickname:",
            "$nickname,",
            nickname
        ).map { it.lowercase() }

        return mentionPatterns.any { pattern ->
            message.content.lowercase().contains(pattern)
        }
    }

    private fun observeMessages() {
        viewModelScope.launch {
            chatViewModel.conversations.collectLatest { conversations ->
                connectionViewModel.serverState.collectLatest { serverState ->
                    val nickname = serverState.nickname // Recuperar el nickname del estado actual

                    if (!isAppInForeground && connectionViewModel.connectionState.value is ConnectionState.Connected) {
                        conversations.forEach { conversation ->
                            conversation.messages.lastOrNull()?.let { message ->
                                if (!message.isOwnMessage) {
                                    when (conversation.type) {
                                        ConversationType.PRIVATE_MESSAGE -> {
                                            notificationService.showPrivateMessageNotification(
                                                message = message,
                                                conversation = conversation
                                            )
                                        }
                                        ConversationType.CHANNEL -> {
                                            if (isMentioned(message, nickname)) {
                                                notificationService.showMentionNotification(
                                                    message = message,
                                                    conversation = conversation
                                                )
                                            }
                                        }
                                        else -> {} // Ignorar otros tipos de conversación
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
