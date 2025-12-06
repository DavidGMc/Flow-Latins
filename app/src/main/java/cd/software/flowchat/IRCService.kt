package cd.software.flowchat

import android.content.Context
import cd.software.flowchat.irc.commands.IRCCommandExecutor
import cd.software.flowchat.irc.config.IRCConfigurationBuilder
import cd.software.flowchat.irc.connection.IRCConnectionManager
import cd.software.flowchat.irc.conversation.ConversationManager
import cd.software.flowchat.irc.events.IRCEventHandler
import cd.software.flowchat.irc.message.IRCMessageHandler
import cd.software.flowchat.model.Channel
import cd.software.flowchat.model.ChannelUser
import cd.software.flowchat.model.ConnectionState
import cd.software.flowchat.model.Conversation
import cd.software.flowchat.model.IRCConnectionInfo
import cd.software.flowchat.model.IRCMessage
import cd.software.flowchat.preferences.ChatPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.pircbotx.PircBotX

/**
 * Servicio principal de IRC que gestiona la conexión al servidor y la sincronización de datos en
 * tiempo real.
 *
 * Este servicio realiza las siguientes tareas de sincronización de datos
 * (FOREGROUND_SERVICE_DATA_SYNC):
 * 1. Mantiene una conexión TCP persistente con el servidor IRC
 * 2. Procesa y sincroniza mensajes entrantes en tiempo real
 * 3. Sincroniza listas de canales y usuarios
 * 4. Maneja eventos del servidor (joins, parts, kicks, etc.)
 * 5. Envía mensajes y comandos al servidor
 * 6. Implementa reconexión automática para mantener el servicio activo
 *
 * La sincronización ocurre incluso cuando la app está en segundo plano, proporcionando una
 * experiencia de chat ininterrumpida al usuario.
 *
 * REFACTORIZADO: Esta clase ahora delega responsabilidades a componentes especializados para
 * mejorar la mantenibilidad y escalabilidad.
 */
class IRCService(private val chatPreferences: ChatPreferences) {

    // ========== Scope de Coroutines ==========

    private var serviceJob = SupervisorJob()
    private var serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private fun resetServiceScope() {
        if (serviceJob.isCancelled || !serviceJob.isActive) {
            val newJob = SupervisorJob()
            serviceScope = CoroutineScope(Dispatchers.IO + newJob)
        }
    }

    // ========== Componentes Especializados ==========

    private val conversationManager = ConversationManager()

    private lateinit var messageHandler: IRCMessageHandler
    private lateinit var eventHandler: IRCEventHandler
    private lateinit var configBuilder: IRCConfigurationBuilder
    private lateinit var connectionManager: IRCConnectionManager
    private lateinit var commandExecutor: IRCCommandExecutor

    private var isInitialized = false

    /** Inicializa los componentes que requieren contexto. */
    private fun initializeComponents(context: Context) {
        if (isInitialized) return

        messageHandler = IRCMessageHandler(chatPreferences, context)

        eventHandler =
                IRCEventHandler(
                        messageHandler = messageHandler,
                        conversationManager = conversationManager,
                        chatPreferences = chatPreferences,
                        applicationContext = context,
                        serviceScope = serviceScope,
                        onConnectionStateChange = { update ->
                            connectionManager.handleConnectionStateUpdate(update)
                        },
                        getBotNick = { connectionManager.getBotNick() },
                        sendRawLine = { line -> connectionManager.sendRawLine(line) }
                )

        configBuilder = IRCConfigurationBuilder(eventHandler)

        connectionManager =
                IRCConnectionManager(
                        configBuilder = configBuilder,
                        conversationManager = conversationManager,
                        messageHandler = messageHandler,
                        serviceScope = serviceScope
                )

        commandExecutor =
                IRCCommandExecutor(
                        connectionManager = connectionManager,
                        conversationManager = conversationManager,
                        serviceScope = serviceScope
                )

        isInitialized = true
    }

    // ========== API Pública - Estado ==========

    val connectionState: StateFlow<ConnectionState>
        get() = connectionManager.connectionState

    val conversations: StateFlow<List<Conversation>>
        get() = conversationManager.conversations

    val syncState: StateFlow<String>
        get() = connectionManager.syncState

    val availableChannels: StateFlow<List<Channel>>
        get() = conversationManager.availableChannels

    val channelRemovalConfirmed: StateFlow<String?>
        get() = conversationManager.channelRemovalConfirmed

    var bot: PircBotX?
        get() = connectionManager.bot
        set(_) {} // No-op, mantenido por compatibilidad

    // ========== API Pública - Configuración ==========

    fun setApplicationContext(context: Context) {
        initializeComponents(context)
        connectionManager.setApplicationContext(context)
    }

    // ========== API Pública - Conexión ==========

    fun connect(connectionInfo: IRCConnectionInfo) {
        resetServiceScope()
        serviceScope.launch { connectionManager.connect(connectionInfo) }
    }

    fun disconnect() {
        serviceScope.launch { connectionManager.disconnect() }
    }

    fun reconnectIfNeeded(connectionInfo: IRCConnectionInfo) {
        connectionManager.reconnectIfNeeded(connectionInfo)
    }

    fun isConnected(): Boolean {
        return connectionManager.isConnected()
    }

    // ========== API Pública - Canales ==========

    fun joinChannel(channel: String) {
        commandExecutor.joinChannel(channel)
    }

    fun partChannel(channelName: String) {
        commandExecutor.partChannel(channelName)
    }

    fun removeChannelFromConversations(channelName: String) {
        conversationManager.removeChannel(channelName)
    }

    fun fetchAvailableChannels() {
        commandExecutor.fetchAvailableChannels()
    }

    fun channelExists(channelName: String): Boolean {
        return commandExecutor.channelExists(channelName)
    }

    fun getUsersForChannel(channelName: String): List<ChannelUser> {
        return conversationManager.getUsersForChannel(channelName, connectionManager.bot)
    }

    fun setTopic(channel: String, topic: String, onResult: (Boolean, String?) -> Unit) {
        commandExecutor.setTopic(channel, topic, onResult)
    }

    // ========== API Pública - Mensajes ==========

    fun sendMessage(conversation: Conversation, message: String) {
        commandExecutor.sendMessage(conversation, message)
    }

    fun sendPrivateMessage(nickname: String, message: String) {
        commandExecutor.sendPrivateMessage(nickname, message)
    }

    fun sendNotice(target: String, message: String, currentChannelName: String? = null) {
        commandExecutor.sendNotice(target, message, currentChannelName)
    }

    fun sendAction(target: String, action: String) {
        commandExecutor.sendAction(target, action)
    }
    fun addMessage(message: IRCMessage) {
        conversationManager.addMessage(message)
    }

    // ========== API Pública - Conversaciones ==========

    fun startPrivateConversation(nickname: String): Conversation {
        return conversationManager.startPrivateConversation(nickname)
    }

    fun removePrivateConversation(nickname: String) {
        conversationManager.removePrivateConversation(nickname)
    }

    fun startServiceConversation(serviceName: String) {
        conversationManager.startServiceConversation(serviceName)
    }

    fun handleAutoJoinChannel(channelName: String) {
        conversationManager.handleAutoJoinChannel(channelName)
    }

    fun checkForMention(message: String, username: String): Boolean {
        return messageHandler.checkForMention(message, username)
    }

    // ========== API Pública - Usuario ==========

    fun changeNick(newNick: String): Boolean {
        return commandExecutor.changeNick(newNick)
    }

    fun requestWhois(nickname: String) {
        commandExecutor.requestWhois(nickname)
    }

    // ========== API Pública - Moderación ==========

    fun opUser(channel: String, nickname: String) {
        commandExecutor.opUser(channel, nickname)
    }

    fun deopUser(channel: String, nickname: String) {
        commandExecutor.deopUser(channel, nickname)
    }

    fun voiceUser(channel: String, nickname: String) {
        commandExecutor.voiceUser(channel, nickname)
    }

    fun devoiceUser(channel: String, nickname: String) {
        commandExecutor.devoiceUser(channel, nickname)
    }

    fun banUser(channel: String, hostmask: String) {
        commandExecutor.banUser(channel, hostmask)
    }

    fun unbanUser(channel: String, hostmask: String) {
        commandExecutor.unbanUser(channel, hostmask)
    }

    fun inviteUser(nickname: String, channel: String) {
        commandExecutor.inviteUser(nickname, channel)
    }

    fun setMode(channel: String, mode: String) {
        commandExecutor.setMode(channel, mode)
    }

    fun requestBanList(channel: String) {
        commandExecutor.requestBanList(channel)
    }

    // ========== API Pública - Servicios IRC ==========

    fun sendServiceCommand(service: String, command: String) {
        commandExecutor.sendServiceCommand(service, command)
    }

    fun sendNickServCommand(command: String) {
        commandExecutor.sendNickServCommand(command)
    }

    fun sendChanServCommand(command: String) {
        commandExecutor.sendChanServCommand(command)
    }

    // ========== API Pública - Comandos Raw ==========

    fun sendRawMessage(rawCommand: String) {
        commandExecutor.sendRawCommand(rawCommand)
    }

    fun sendRawCommand(command: String) {
        commandExecutor.sendRawCommand(command)
    }

    fun executeServerCommand(command: String, arguments: String) {
        commandExecutor.executeServerCommand(command, arguments)
    }

    fun executeServerCommand(rawCommand: String) {
        commandExecutor.executeServerCommand(rawCommand)
    }

    fun whisper(target: String, message: String) {
        commandExecutor.whisper(target, message)
    }

    // ========== API Pública - Limpieza ==========

    fun cleanup() {
        serviceJob.cancel()
    }
}
