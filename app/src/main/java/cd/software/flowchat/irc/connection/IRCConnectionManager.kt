package cd.software.flowchat.irc.connection

import android.content.Context
import android.content.Intent
import android.util.Log
import cd.software.flowchat.ConnectionService
import cd.software.flowchat.irc.config.IRCConfigurationBuilder
import cd.software.flowchat.irc.conversation.ConversationManager
import cd.software.flowchat.irc.events.ConnectionStateUpdate
import cd.software.flowchat.irc.message.IRCMessageHandler
import cd.software.flowchat.model.ConnectionState
import cd.software.flowchat.model.IRCConnectionInfo
import es.chat.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.pircbotx.PircBotX

/**
 * Gestiona el ciclo de vida de la conexión IRC. Responsable de conectar, desconectar y mantener el
 * estado de la conexión.
 */
class IRCConnectionManager(
        private val configBuilder: IRCConfigurationBuilder,
        private val conversationManager: ConversationManager,
        private val messageHandler: IRCMessageHandler,
        private val serviceScope: CoroutineScope
) {

    private var applicationContext: Context? = null

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _syncState = MutableStateFlow<String>("INACTIVE")
    val syncState: StateFlow<String> = _syncState.asStateFlow()

    var bot: PircBotX? = null
        private set

    /** Configura el contexto de la aplicación. */
    fun setApplicationContext(context: Context) {
        this.applicationContext = context.applicationContext

        // Actualizar estado actual
        val currentState = _connectionState.value
        if (currentState !is ConnectionState.Disconnected) {
            updateConnectionState(currentState)
        }
    }

    /** Conecta al servidor IRC. */
    suspend fun connect(connectionInfo: IRCConnectionInfo) {
        val context = applicationContext ?: return

        _syncState.value = context.getString(R.string.sync_state_starting)
        _connectionState.value = ConnectionState.Connecting
        updateConnectionState(ConnectionState.Connecting)

        val connectMessage =
                context.getString(
                        R.string.connection_starting,
                        connectionInfo.serverConfig.url,
                        connectionInfo.serverConfig.port
                )

        conversationManager.getOrCreateStatusConversation()
        conversationManager.addMessage(messageHandler.createSystemSyncMessage(connectMessage))

        serviceScope.launch {
            val configuration = configBuilder.buildConfiguration(connectionInfo)

            try {
                withContext(Dispatchers.IO) {
                    bot = PircBotX(configuration)
                    updateConnectionState(ConnectionState.Connecting)

                    // Intentar autenticar después de conectar
                    bot?.startBot()

                    // Enviar autenticación si hay contraseña
                    if (connectionInfo.password.isNotBlank()) {
                        try {
                            bot?.sendRaw()
                                    ?.rawLine(
                                            "PRIVMSG NICKSERV IDENTIFY ${connectionInfo.nickname} ${connectionInfo.password}"
                                    )
                        } catch (e: Exception) {
                            Log.e(
                                    "IRCConnectionManager",
                                    "Error al enviar autenticación: ${e.message}"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                val errorState = ConnectionState.Error(e.message ?: "Connection error")
                _connectionState.value = errorState
                _syncState.value = "ERROR"

                updateConnectionState(errorState)

                val errorMessage = "Error de conexión: ${e.message}"
                conversationManager.addMessage(messageHandler.createSystemSyncMessage(errorMessage))

                Log.e("IRCConnectionManager", "Error de conexión", e)
            }
        }
    }

    /** Desconecta del servidor IRC. */
    suspend fun disconnect() {
        serviceScope.launch {
            try {
                bot?.stopBotReconnect()
                bot?.send()?.quitServer("Goodbye from FlowIRC Android!")
                bot?.close()
            } catch (e: Exception) {
                Log.e("IRCConnectionManager", "Error al desconectar: ${e.message}")
            } finally {
                _connectionState.value = ConnectionState.Disconnected
                _syncState.value = "DISCONNECTED"

                conversationManager.clearAvailableChannels()
                conversationManager.clearAllExceptStatus()

                bot = null
            }
        }
    }

    /** Reconecta si es necesario. */
    fun reconnectIfNeeded(connectionInfo: IRCConnectionInfo) {
        if (bot == null || !isConnected()) {
            serviceScope.launch { connect(connectionInfo) }
        }
    }

    /** Verifica si está conectado. */
    fun isConnected(): Boolean {
        return bot?.isConnected == true && _connectionState.value is ConnectionState.Connected
    }

    /** Actualiza el estado de conexión desde el event handler. */
    fun handleConnectionStateUpdate(update: ConnectionStateUpdate) {
        when (update) {
            is ConnectionStateUpdate.Connected -> {
                _connectionState.value = ConnectionState.Connected
                _syncState.value = "ACTIVE"
                updateConnectionState(ConnectionState.Connected)
            }
            is ConnectionStateUpdate.Connecting -> {
                _connectionState.value = ConnectionState.Connecting
                _syncState.value = "CONNECTING"
                updateConnectionState(ConnectionState.Connecting)
            }
            is ConnectionStateUpdate.Disconnected -> {
                _connectionState.value = ConnectionState.Disconnected
                _syncState.value = "DISCONNECTED"
                updateConnectionState(ConnectionState.Disconnected)
            }
            is ConnectionStateUpdate.Error -> {
                val errorState = ConnectionState.Error(update.message)
                _connectionState.value = errorState
                _syncState.value = "ERROR"
                updateConnectionState(errorState)
            }
        }
    }

    /** Actualiza el estado de conexión y notifica al servicio. */
    private fun updateConnectionState(state: ConnectionState) {
        _connectionState.value = state

        val context = applicationContext ?: return

        val stateString =
                when (state) {
                    is ConnectionState.Connected -> "CONNECTED"
                    is ConnectionState.Connecting -> "CONNECTING"
                    is ConnectionState.Disconnected -> "DISCONNECTED"
                    is ConnectionState.Error -> "ERROR"
                    else -> "UNKNOWN"
                }

        // Enviar intent al servicio
        val serviceIntent =
                Intent(context, ConnectionService::class.java).apply {
                    action = ConnectionService.ACTION_UPDATE_STATUS
                    putExtra(ConnectionService.EXTRA_CONNECTION_STATE, stateString)
                    putExtra(ConnectionService.EXTRA_SYNC_STATE, _syncState.value)
                }
        context.startService(serviceIntent)

        // Enviar broadcast
        val broadcastIntent =
                Intent(ConnectionService.ACTION_UPDATE_STATUS).apply {
                    putExtra(ConnectionService.EXTRA_CONNECTION_STATE, stateString)
                    putExtra(ConnectionService.EXTRA_SYNC_STATE, _syncState.value)
                }
        context.sendBroadcast(broadcastIntent)

        Log.d(
                "IRCConnectionManager",
                "Estado actualizado: $stateString, syncState=${_syncState.value}"
        )
    }

    /** Obtiene el nickname actual del bot. */
    fun getBotNick(): String? = bot?.nick

    /** Envía una línea raw al servidor. */
    fun sendRawLine(line: String) {
        bot?.sendRaw()?.rawLine(line)
    }
}
