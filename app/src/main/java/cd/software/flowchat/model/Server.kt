package cd.software.flowchat.model

data class Server(
    val hostname: String,
    val port: Int,
    val nickname: String,
    val channels: List<Channel> = emptyList(), // Canales a los que está unido.
    val status: ServerStatus = ServerStatus.DISCONNECTED
)
