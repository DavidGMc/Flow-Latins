package cd.software.flowchat.model

data class ServerConnectionState (
    val server: String = "irc.chateamos.org",
    val port: Int = 6667,
    val nickname: String = "",
    val password: String = "",
    val useSSL: Boolean = false
)