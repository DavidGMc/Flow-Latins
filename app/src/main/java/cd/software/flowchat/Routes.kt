package cd.software.flowchat

import androidx.annotation.Keep

@Keep
sealed class Routes(val route: String) {
    data object Connection : Routes("connection")
    data object Chat : Routes("chat")
    data object Channels : Routes("channels")
    data object Users : Routes("users")
    data object AddServer : Routes("add_server")
    data object EditServer : Routes("edit_server/{serverUrl}") {
        fun createRoute(serverUrl: String) = "edit_server/$serverUrl"
    }
    data object Profile : Routes("profile/{userId}") {
        fun createRoute(userId: String) = "profile/$userId"
    }
    data object Login : Routes("login")
    data object Registration : Routes("registration")
    data object Settings : Routes("settings/{currentChannel}") {
        fun createRoute(currentChannel: String) = "settings/$currentChannel"
    }
    // Rutas con argumentos para notificaciones
    data object PrivateChat : Routes("chat/private/{username}") {
        fun createRoute(username: String) = "chat/private/$username"
    }
    data object ChannelChat : Routes("chat/channel/{channelName}") {
        fun createRoute(channelName: String) = "chat/channel/$channelName"
    }
    object Community : Routes("community")

    object UserDetails : Routes("user_details/{userId}?isUsername={isUsername}") {
        fun createRoute(userId: String) = "user_details/$userId?isUsername=false"
        fun createUsernameRoute(username: String) = "user_details/$username?isUsername=true"
    }
    object WebContent : Routes("webcontent")
    object ColorSettings : Routes("color_settings")


    companion object {
        fun fromString(route: String): Routes {
            return when {
                route.startsWith("chat/private/") -> PrivateChat
                route.startsWith("chat/channel/") -> ChannelChat
                route == Connection.route -> Connection
                route == Chat.route -> Chat
                route == Channels.route -> Channels
                route == Users.route -> Users
                route == AddServer.route -> AddServer
                else -> throw IllegalArgumentException("Ruta desconocida: $route")

            }
        }
    }
}