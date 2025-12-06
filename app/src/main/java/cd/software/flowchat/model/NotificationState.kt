package cd.software.flowchat.model

sealed class NotificationState {
    object Idle : NotificationState()
    object Connected : NotificationState()
    object Disconnected : NotificationState()
    data class Mentioned(val nickname: String, val channel: String) : NotificationState()
    data class PrivateMessage(val sender: String) : NotificationState()
}