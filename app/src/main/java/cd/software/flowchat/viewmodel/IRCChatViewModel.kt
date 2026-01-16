package cd.software.flowchat.viewmodel


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cd.software.flowchat.IRCService
import cd.software.flowchat.MainActivity
import cd.software.flowchat.NotificationService
import cd.software.flowchat.model.Channel
import cd.software.flowchat.model.Conversation
import cd.software.flowchat.model.ConversationType
import cd.software.flowchat.model.IRCMessage
import cd.software.flowchat.preferences.ChatPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.content.Intent
import android.util.Log
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import cd.software.flowchat.bannerwords.ContentFilter
import cd.software.flowchat.commands.CommandManager
import cd.software.flowchat.commands.CommandRegistry
import cd.software.flowchat.mircolors.IRCColorPreferences
import cd.software.flowchat.mircolors.IRCColors
import cd.software.flowchat.mircolors.IRCMessageComposer
import cd.software.flowchat.mircolors.IRCMessageFormatter
import cd.software.flowchat.model.ChannelUser

import cd.software.flowchat.model.MessageType
import cd.software.flowchat.model.CustomPagerState

import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

class IRCChatViewModel(
    val ircService: IRCService,
    val notificationService: NotificationService,
    val chatPreferences: ChatPreferences,
    private val context: Context
) : ViewModel() {
    // Estados principales
    private val _conversations = MutableStateFlow<List<Conversation>>(
        listOf(
            Conversation(
                name = "Status",
                type = ConversationType.SERVER_STATUS
            )
        )
    )
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _currentConversationIndex = MutableStateFlow(0)
    val currentConversationIndex: StateFlow<Int> = _currentConversationIndex.asStateFlow()

    // Estado unificado de navegación
    data class NavigationState(
        val targetPage: Int? = null,
        val isProgrammatic: Boolean = false,
        val isLoading: Boolean = false
    )
    private val _navigationState = MutableStateFlow(NavigationState())
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()

    // Estados secundarios
    private val _usersInCurrentChannel = MutableStateFlow<List<ChannelUser>>(emptyList())
    val usersInCurrentChannel = _usersInCurrentChannel.asStateFlow()

    val availableChannels: StateFlow<List<Channel>> = ircService.availableChannels
    private val _notifications = MutableStateFlow<List<String>>(emptyList())
    val notifications: StateFlow<List<String>> = _notifications.asStateFlow()
    private val _currentChannel = MutableStateFlow("")
    val currentChannel: StateFlow<String> = _currentChannel.asStateFlow()
    private val _messageInput = MutableStateFlow("")
    val messageInput = _messageInput.asStateFlow()
    private val _reportActionComplete = MutableStateFlow(false)
    val reportActionComplete: StateFlow<Boolean> = _reportActionComplete
    private val _lastClosedPrivateChat = MutableStateFlow<String?>(null)
    val lastClosedPrivateChat: StateFlow<String?> = _lastClosedPrivateChat.asStateFlow()

    // Tracking de mensajes notificados para evitar duplicados
    private val notifiedMessageIds = mutableSetOf<String>()
    private val conversationLastVisitTimestamps = mutableMapOf<String, Long>()

    // Preferencias
    val ircColorPreferences = IRCColorPreferences(chatPreferences)
    val useColors: StateFlow<Boolean> = ircColorPreferences.useColors
    val defaultColor: StateFlow<Int> = ircColorPreferences.defaultColor
    val backgroundColor: StateFlow<Int> = ircColorPreferences.getBackgroundColor()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val contentFilter = ContentFilter()
    val showJoinEvents = chatPreferences.showJoinEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)
    val showQuitEvents = chatPreferences.showQuitEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
    val showPartEvents = chatPreferences.showPartEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
    val showBanEvents = chatPreferences.showBanEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
    val fontSize = chatPreferences.fontSize
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 14)
    val compactMessageFormat = chatPreferences.compactMessageFormat.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )
    val ignoredUsers = chatPreferences.ignoredUsers.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        emptySet()
    )

    // Sistema de comandos
    private val commandManager = CommandManager()

    init {
        // Registrar comandos
        CommandRegistry.registerAllCommands(commandManager, context)

        // Observar conversaciones del IRC
        viewModelScope.launch {
            ircService.conversations.collect { updatedConversations ->
                if (updatedConversations.isNotEmpty()) {
                    val serverStatusConversation = updatedConversations.find {
                        it.type == ConversationType.SERVER_STATUS
                    }

                    _conversations.value = if (serverStatusConversation != null) {
                        listOf(serverStatusConversation) +
                                updatedConversations.filter { it.type != ConversationType.SERVER_STATUS }
                    } else {
                        updatedConversations
                    }

                    // Verificar notificaciones solo para mensajes nuevos
                    updatedConversations.forEach { conversation ->
                        // Solo verificar el último mensaje de cada conversación
                        conversation.messages.lastOrNull()?.let { message ->
                            checkAndNotify(message, conversation)
                        }
                    }

                }
            }
        }
    }


    fun switchToConversation(index: Int, fromUser: Boolean = false) {
        if (index in _conversations.value.indices) {
            // Actualizar inmediatamente el índice
            _currentConversationIndex.value = index

            _navigationState.update {
                it.copy(
                    targetPage = index,
                    isProgrammatic = !fromUser
                )
            }
        }
    }

    fun onPagerSettled(pageIndex: Int) {
        _navigationState.update {
            if (it.targetPage == pageIndex) it.copy(targetPage = null) else it
        }

        if (pageIndex != _currentConversationIndex.value) {
            _currentConversationIndex.value = pageIndex
        }
    }

    // Función mejorada para chats privados
    // En IRCChatViewModel
    fun startPrivateConversationAndNavigate(nickname: String) {

        viewModelScope.launch {
            _navigationState.update { it.copy(isLoading = true) }

            try {
                val isBlocked = chatPreferences.isUserBlocked(nickname).first()
                val blockPrivate = chatPreferences.blockPrivateMessages.first()

                if (!isBlocked && !blockPrivate) {
                    if (!conversations.value.any { it.name == nickname && it.type == ConversationType.PRIVATE_MESSAGE }) {
                        ircService.startPrivateConversation(nickname)
                    }

                    val newIndex = withTimeoutOrNull(2000) {
                        conversations
                            .map { convs ->
                                convs.indexOfFirst {
                                    it.name == nickname && it.type == ConversationType.PRIVATE_MESSAGE
                                }
                            }
                            .first { it != -1 }
                    } ?: run {
                        Log.e("Navigation", "Timeout waiting for private chat creation")
                        return@launch
                    }

                    switchToConversation(newIndex, fromUser = false)
                    _conversations.value = conversations.value.toList()
                }
            } catch (e: Exception) {
                Log.e("Navigation", "Error opening private chat", e)
            } finally {
                _navigationState.update { it.copy(isLoading = false) }
            }
        }
    }
    fun startPrivateConversationFromUsers(nickname: String) {
        viewModelScope.launch {
            val isBlocked = chatPreferences.isUserBlocked(nickname).first()
            val blockPrivate = chatPreferences.blockPrivateMessages.first()

            if (!isBlocked && !blockPrivate) {
                // Verificar si ya existe la conversación
                val existingIndex = conversations.value.indexOfFirst {
                    it.name == nickname && it.type == ConversationType.PRIVATE_MESSAGE
                }

                if (existingIndex != -1) {
                    // Si ya existe, solo navegar a ella
                    switchToConversation(existingIndex, fromUser = false)
                    clearLastClosedPrivateChat()
                } else {
                    // Si no existe, crearla y navegar
                    ircService.startPrivateConversation(nickname)
                    clearLastClosedPrivateChat()

                    // Esperar a que se cree la conversación y navegar hacia ella
                    val newIndex = withTimeoutOrNull(2000) {
                        conversations
                            .map { convs ->
                                convs.indexOfFirst {
                                    it.name == nickname && it.type == ConversationType.PRIVATE_MESSAGE
                                }
                            }
                            .first { it != -1 }
                    } ?: run {
                        Log.e("Navigation", "Timeout waiting for private chat creation from users list")
                        return@launch
                    }

                    // Navegar a la nueva conversación
                    switchToConversation(newIndex, fromUser = false)
                }
            }
        }
    }



    // Funciones existentes actualizadas
    fun joinChannelFromListAndNavigate(channelName: String) {
        viewModelScope.launch {
            _navigationState.update { it.copy(isLoading = true) }

            try {
                ircService.joinChannel(channelName)

                val newConversation = Conversation(
                    name = channelName,
                    type = ConversationType.CHANNEL
                )

                _conversations.update { currentConversations ->
                    if (currentConversations.none { it.name == channelName }) {
                        currentConversations + newConversation
                    } else {
                        currentConversations
                    }
                }

                val newIndex = _conversations.value.indexOfFirst { it.name == channelName }
                if (newIndex != -1) {
                    switchToConversation(newIndex, fromUser = false)
                }
            } finally {
                _navigationState.update { it.copy(isLoading = false) }
            }
        }
    }

    // Resto de funciones (sin cambios en su lógica interna)
    fun updateMessageInput(newInput: String) {
        _messageInput.value = newInput
    }

    // En IRCChatViewModel
    /*fun sendMessage(messageText: String? = null) {
        viewModelScope.launch {
            val message = messageText ?: _messageInput.value.trim()
            if (message.isEmpty()) return@launch

            // Obtener la conversación ACTUAL según el índice
            val currentConversation = getCurrentConversation() ?: run {
                Log.e("SendMessage", "No hay conversación activa")
                return@launch
            }

            _messageInput.value = ""

            if (currentConversation.type == ConversationType.SERVER_STATUS && !message.startsWith("/")) {
                addSystemMessage(IRCMessage(
                    sender = "System",
                    content = "No puedes enviar mensajes en esta sala. Solo se permiten comandos.",
                    conversationType = ConversationType.SERVER_STATUS,
                    type = MessageType.TEXT,
                    channelName = "Status"
                ))
                return@launch
            }


            // 1. PRIMERO verificar si es un comando (incluso si tiene prefijos de formato)
            val cleanCommand = if (message.startsWith(IRCColors.COLOR_CHAR)) {
                // Si empieza con código de color, extraer el comando después del espacio
                message.substringAfter(' ').takeIf { it.startsWith("/") } ?: ""
            } else {
                message.takeIf { it.startsWith("/") } ?: ""
            }

            if (cleanCommand.isNotEmpty()) {
                // Procesar como comando
                val (isCommand, commandResult) = commandManager.processMessage(
                    cleanCommand,
                    ircService,
                    currentConversation
                )

                if (isCommand) {
                    if (commandResult != null) {
                        addSystemMessage(commandResult)
                    }
                    return@launch
                }
                // Si no es un comando reconocido, continuar como mensaje normal
            }

            // 2. Para mensajes normales (no comandos)
            ircService.sendMessage(currentConversation, message)
        }
    }*/
    fun sendMessage(messageText: String? = null) {
        viewModelScope.launch {
            val message = messageText ?: _messageInput.value.trim()
            if (message.isEmpty()) return@launch

            // Usar la conversación local sincronizada
            val currentIndex = _currentConversationIndex.value
            val currentConversation = _conversations.value.getOrNull(currentIndex) ?: run {
                Log.e("SendMessage", "No hay conversación activa en índice $currentIndex")
                return@launch
            }

            _messageInput.value = ""

            if (currentConversation.type == ConversationType.SERVER_STATUS && !message.startsWith("/")) {
                addSystemMessage(
                    IRCMessage(
                        sender = "System",
                        content = "No puedes enviar mensajes en esta sala. Solo se permiten comandos.",
                        conversationType = ConversationType.SERVER_STATUS,
                        type = MessageType.TEXT,
                        channelName = "Status"
                    )
                )
                return@launch
            }

            // Procesar comandos
            val cleanCommand = if (message.startsWith(IRCColors.COLOR_CHAR)) {
                message.substringAfter(' ').takeIf { it.startsWith("/") } ?: ""
            } else {
                message.takeIf { it.startsWith("/") } ?: ""
            }

            if (cleanCommand.isNotEmpty()) {
                val (isCommand, commandResult) = commandManager.processMessage(
                    cleanCommand,
                    ircService,
                    currentConversation
                )

                if (isCommand) {
                    if (commandResult != null) {
                        addSystemMessage(commandResult)
                    }
                    return@launch
                }
            }

            // Leer preferencias directamente
            val useColors = ircColorPreferences.useColors.first()
            val disableColorCodes = ircColorPreferences.getDisableColorCodes().first()

            // Aplicar colores si está habilitado en preferencias
            val finalMessage = if (useColors && !disableColorCodes) {
                val defaultColor = ircColorPreferences.defaultColor.first()
                val backgroundColor = ircColorPreferences.backgroundColor.first()

                // Aplicar colores usando IRCMessageComposer
                IRCMessageComposer()
                    .addColoredText(message, defaultColor, backgroundColor)
                    .build()
            } else {
                message
            }

            // Enviar mensaje normal o con colores
            ircService.sendMessage(currentConversation, finalMessage)
        }
    }

    private fun addSystemMessage(message: IRCMessage) {
        ircService.addMessage(message)
    }

    fun getCurrentConversation(): Conversation? {
        return _conversations.value.getOrNull(_currentConversationIndex.value)
    }

    fun startPrivateConversation(nickname: String) {
        viewModelScope.launch {
            val isBlocked = chatPreferences.isUserBlocked(nickname).first()
            val blockPrivate = chatPreferences.blockPrivateMessages.first()

            if (!isBlocked && !blockPrivate) {
                ircService.startPrivateConversation(nickname)
            }
        }
    }


    fun sendPrivateMessage(nickname: String, message: String) {
        viewModelScope.launch {
            val isBlocked = chatPreferences.isUserBlocked(nickname).first()
            val blockPrivate = chatPreferences.blockPrivateMessages.first()

            if (!isBlocked && !blockPrivate) {
                ircService.sendPrivateMessage(nickname, message)
            }
        }
    }

    fun removeConversation(conversation: Conversation) {
        if (conversation.type == ConversationType.SERVER_STATUS) return

        val updatedConversations = _conversations.value.filter { it != conversation }
        val newIndex = when {
            _currentConversationIndex.value >= updatedConversations.size ->
                maxOf(0, updatedConversations.size - 1)
            else -> _currentConversationIndex.value
        }

        _conversations.value = updatedConversations
        _currentConversationIndex.value = newIndex
    }

    /* fun exitCurrentConversation() {
         val currentConversation = conversations.value.getOrNull(currentConversationIndex.value) ?: return

         when (currentConversation.type) {
             ConversationType.PRIVATE_MESSAGE -> {
                 // 1. Guardar el nombre del chat cerrado
                 _lastClosedPrivateChat.value = currentConversation.name

                 // 2. Solo eliminar esta conversación privada
                 _conversations.update { current ->
                     current.filterNot { it == currentConversation }
                 }

                 // 3. Mover el índice a una posición segura (sin cambiar salas/canales)
                 val newIndex = if (_conversations.value.isNotEmpty()) {
                     minOf(_currentConversationIndex.value, _conversations.value.size - 1)
                 } else {
                     0
                 }
                 _currentConversationIndex.value = newIndex

                 // 4. Limpiar el chat privado del servicio IRC
                 ircService.removePrivateConversation(currentConversation.name)
             }
             ConversationType.CHANNEL -> {
                 // 1. Salir del canal en el servicio IRC
                 ircService.partChannel(currentConversation.name)
                 // Después de salir del canal, eliminar la conversación
                 ircService.removeChannelFromConversations(currentConversation.name)

                 // 2. Eliminar la conversación del canal de la lista
                 _conversations.update { current ->
                     current.filterNot { it == currentConversation }
                 }

                 // 3. Ajustar el índice para evitar estar fuera de rango
                 val newIndex = if (_conversations.value.isNotEmpty()) {
                     minOf(_currentConversationIndex.value, _conversations.value.size - 1)
                 } else {
                     0
                 }
                 _currentConversationIndex.value = newIndex
             }
             else -> {} // No hacer nada para otros tipos (como SERVER_STATUS)
         }

     }*/

    fun exitCurrentConversation() {
        val currentConversation = conversations.value.getOrNull(currentConversationIndex.value) ?: return

        when (currentConversation.type) {
            ConversationType.PRIVATE_MESSAGE -> {
                // 1. Guardar el nombre del chat cerrado
                _lastClosedPrivateChat.value = currentConversation.name

                // 2. Solo eliminar esta conversación privada
                _conversations.update { current ->
                    current.filterNot { it == currentConversation }
                }

                // 3. Mover el índice a una posición segura (sin cambiar salas/canales)
                val newIndex = if (_conversations.value.isNotEmpty()) {
                    minOf(_currentConversationIndex.value, _conversations.value.size - 1)
                } else {
                    0
                }
                _currentConversationIndex.value = newIndex

                // 4. Limpiar el chat privado del servicio IRC
                ircService.removePrivateConversation(currentConversation.name)
            }
            ConversationType.CHANNEL -> {
                // Lógica existente para canales (no cambiar)
                ircService.partChannel(currentConversation.name)
                ircService.removeChannelFromConversations(currentConversation.name)
            }
            else -> {} // No hacer nada para otros tipos
        }



    }
    fun clearLastClosedPrivateChat() {
        viewModelScope.launch {
            _lastClosedPrivateChat.value = ""
        }
    }


    fun setLastClosedPrivateChat(username: String?) {
        viewModelScope.launch {
            _lastClosedPrivateChat.value = username
        }
    }
    private suspend fun awaitConversationAdded(nickname: String): Int {
        return withTimeoutOrNull(3000) {
            _conversations
                .map { conversations ->
                    conversations.indexOfFirst {
                        it.name == nickname && it.type == ConversationType.PRIVATE_MESSAGE
                    }
                }
                .first { it != -1 }
        } ?: -1
    }

    fun joinChannelFromList(channelName: String) {
        viewModelScope.launch {
            ircService.joinChannel(channelName)
            val newConversation = Conversation(
                name = channelName,
                type = ConversationType.CHANNEL
            )

            _conversations.update { currentConversations ->
                if (currentConversations.none { it.name == channelName }) {
                    currentConversations + newConversation
                } else {
                    currentConversations
                }
            }

            val newIndex = _conversations.value.indexOfFirst { it.name == channelName }
            if (newIndex != -1) {
                _currentConversationIndex.value = newIndex
            }
        }
    }

    fun getUsersForCurrentChannel(): List<String> {
        return usersInCurrentChannel.value.map { it.nickname }
    }

    fun updateUsersInCurrentChannel(users: List<ChannelUser>) {
        _usersInCurrentChannel.value = users
    }

    fun loadAvailableChannels() {
        ircService.fetchAvailableChannels()
    }

    fun loadUsersForCurrentChannel() {
        val currentConversation = getCurrentConversation()
        if (currentConversation?.type == ConversationType.CHANNEL) {
            viewModelScope.launch {
                try {
                    if (ircService.isConnected() && ircService.channelExists(currentConversation.name)) {
                        val users = ircService.getUsersForChannel(currentConversation.name)
                        _usersInCurrentChannel.value = users
                    } else {
                        _usersInCurrentChannel.value = emptyList()
                        Log.d("IRCChatViewModel", "Canal ${currentConversation.name} no existe")
                    }
                } catch (e: Exception) {
                    Log.e("IRCChatViewModel", "Error al cargar usuarios", e)
                    _usersInCurrentChannel.value = emptyList()
                }
            }
        }
    }

    fun clearState() {
        viewModelScope.launch {
            _conversations.value = emptyList()
            _currentConversationIndex.value = 0
            _navigationState.value = NavigationState()
        }
    }

    fun checkAndNotify(message: IRCMessage, conversation: Conversation) {
        // Generar ID único para el mensaje
        val messageId = "${message.timestamp}_${message.sender}_${message.content.hashCode()}"

        // Si ya notificamos este mensaje, salir
        if (messageId in notifiedMessageIds) {
            return
        }

        // Si el mensaje es del propio usuario, no notificar
        if (message.isOwnMessage) {
            return
        }

        // Si la conversación está actualmente activa, no notificar
        if (conversation.name == getCurrentConversation()?.name) {
            // Marcar como notificado para no volver a notificar si cambiamos de tab
            notifiedMessageIds.add(messageId)
            return
        }

        // Verificar si el mensaje es anterior a la última visita a esta conversación
        val lastVisit = conversationLastVisitTimestamps[conversation.name] ?: 0L
        if (message.timestamp <= lastVisit) {
            // Mensaje ya fue visto, no notificar
            notifiedMessageIds.add(messageId)
            return
        }

        // Notificar según el tipo de mensaje
        if (message.isMentioned) {
            _notifications.value =
                _notifications.value + "You were mentioned in ${message.channelName}"
            notificationService.showMentionNotification(message, conversation)
            notifiedMessageIds.add(messageId)
        } else if (message.conversationType == ConversationType.PRIVATE_MESSAGE) {
            _notifications.value =
                _notifications.value + "New private message from ${message.sender}"
            notificationService.showPrivateMessageNotification(message, conversation)
            notifiedMessageIds.add(messageId)
        }
    }

    fun clearNotificationTabSwitch() {
        (notificationService.context as? MainActivity)?.intent?.removeExtra(NotificationService.EXTRA_SHOULD_SWITCH_TAB)
    }

    fun setActiveConversation(conversationName: String?) {
        // Registrar timestamp de visita para prevenir notificaciones duplicadas
        if (conversationName != null) {
            conversationLastVisitTimestamps[conversationName] = System.currentTimeMillis()
            // Cancelar notificaciones de esta conversación cuando se vuelve activa
            notificationService.cancelConversationNotifications(conversationName)
        }
        notificationService.setActiveConversation(conversationName)
    }
    /*fun sendMessageToConversation(messageText: String, conversationIndex: Int) {
        viewModelScope.launch {
            val message = messageText.trim()
            if (message.isEmpty()) return@launch

            val targetConversation = _conversations.value.getOrNull(conversationIndex) ?: run {
                Log.e("SendMessage", "Conversación no encontrada en índice $conversationIndex")
                return@launch
            }

            Log.d("SendMessage", "Enviando mensaje a: ${targetConversation.name} (índice: $conversationIndex)")

            if (targetConversation.type == ConversationType.SERVER_STATUS && !message.startsWith("/")) {
                addSystemMessage(IRCMessage(
                    sender = "System",
                    content = "No puedes enviar mensajes en esta sala. Solo se permiten comandos.",
                    conversationType = ConversationType.SERVER_STATUS,
                    type = MessageType.TEXT,
                    channelName = "Status"
                ))
                return@launch
            }

            // Procesar comandos
            val cleanCommand = if (message.startsWith(IRCColors.COLOR_CHAR)) {
                message.substringAfter(' ').takeIf { it.startsWith("/") } ?: ""
            } else {
                message.takeIf { it.startsWith("/") } ?: ""
            }

            if (cleanCommand.isNotEmpty()) {
                val (isCommand, commandResult) = commandManager.processMessage(
                    cleanCommand,
                    ircService,
                    targetConversation
                )

                if (isCommand) {
                    if (commandResult != null) {
                        addSystemMessage(commandResult)
                    }
                    return@launch
                }
            }

            ircService.sendMessage(targetConversation, message)
        }
    }*/


    val blockPrivateMessages = chatPreferences.blockPrivateMessages.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        false
    )
    val blockedUsers = chatPreferences.blockedUsers.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        emptySet()
    )

    private fun handleIncomingMessage(message: IRCMessage, conversation: Conversation) {
        viewModelScope.launch {
            val isBlocked = chatPreferences.isUserBlocked(message.sender).first()
            val blockPrivate = chatPreferences.blockPrivateMessages.first()
            val isIgnored = chatPreferences.ignoredUsers.first().contains(message.sender)
            val isIgnoredInChannel = if (conversation.type == ConversationType.CHANNEL) {
                chatPreferences.ignoredUsersInChannel(conversation.name).first().contains(message.sender)
            } else false

            if (!isBlocked &&
                !(blockPrivate && message.conversationType == ConversationType.PRIVATE_MESSAGE) &&
                !isIgnored &&
                !isIgnoredInChannel) {
                message.content = contentFilter.filterText(message.content)
            }
        }
    }
    fun setBlockPrivateMessages(block: Boolean) {
        viewModelScope.launch {
            chatPreferences.setBlockPrivateMessages(block)
        }
    }

    fun blockUser(username: String) {
        viewModelScope.launch {
            chatPreferences.blockUser(username)
        }
    }

    fun unblockUser(username: String) {
        viewModelScope.launch {
            chatPreferences.unblockUser(username)
        }
    }

    fun isUserBlocked(username: String): Flow<Boolean> {
        return chatPreferences.blockedUsers.map { it.contains(username) }
    }
    fun ignoreUser(username: String) {
        viewModelScope.launch {
            chatPreferences.ignoreUser(username)
        }
    }
    fun unignoreUser(username: String) {
        viewModelScope.launch {
            chatPreferences.unignoreUser(username)
        }
    }

    fun ignoreUserInChannel(username: String, channel: String) {
        viewModelScope.launch {
            chatPreferences.ignoreUserInChannel(username, channel)
        }
    }

    fun unignoreUserInChannel(username: String, channel: String) {
        viewModelScope.launch {
            chatPreferences.unignoreUserInChannel(username, channel)
        }
    }
    fun ignoredUsersInChannel(channel: String): Flow<Set<String>> {
        return chatPreferences.ignoredUsersInChannel(channel)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptySet())
    }
    val allBlockedAndIgnoredUsers: Flow<Set<String>> = chatPreferences.blockedUsers
        .combine(chatPreferences.ignoredUsers) { blocked, ignored ->
            blocked + ignored
        }
    fun allBlockedAndIgnoredUsers(): Flow<Set<String>> {
        return chatPreferences.blockedUsers
            .combine(chatPreferences.ignoredUsers) { blocked, ignored ->
                blocked + ignored
            }
            .combine(chatPreferences.ignoredUsersInChannel("general")) { global, channelIgnored ->
                global + channelIgnored
            }
    }

    val ignoredUsersInCurrentChannel = currentChannel.flatMapLatest { channel ->
        chatPreferences.ignoredUsersInChannel(channel)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptySet())

    fun reportUser(userId: String, reason: String) {
        viewModelScope.launch {
            try {
                // Crear un intent para abrir el cliente de correo
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"  // Tipo MIME para correo electrónico
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("denuncias@flowirc.com"))
                    putExtra(Intent.EXTRA_SUBJECT, "Denuncia de usuario en FlowIRC")
                    putExtra(Intent.EXTRA_TEXT, """
                    Se ha denunciado al usuario: $userId
                    
                    Motivo de la denuncia:
                    $reason
                    
                    ------------------------------
                    Enviado desde la aplicación FlowIRC
                """.trimIndent())
                }

                // Obtener el contexto desde el servicio de notificaciones
                val context = notificationService.context

                // Verificar si existe alguna aplicación que pueda manejar el intent
                if (context != null && intent.resolveActivity(context.packageManager) != null) {
                    // Agregar la bandera para iniciar una nueva tarea
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    _reportActionComplete.value = true

                    // Reiniciar el estado después de un tiempo
                    delay(3000)
                    _reportActionComplete.value = false
                } else {
                    // Manejar el caso donde no hay cliente de correo
                    Log.e("IRCChatViewModel", "No hay cliente de correo disponible")
                }
            } catch (e: Exception) {
                Log.e("IRCChatViewModel", "Error al abrir el cliente de correo", e)
                _reportActionComplete.value = false
            }
        }
    }
    fun addSystemMessage(message: String) {
        val statusConversation = _conversations.value.find { it.type == ConversationType.SERVER_STATUS }
            ?: return

        val systemMessage = IRCMessage(
            sender = "System",
            content = message,
            conversationType = ConversationType.SERVER_STATUS,
            type = MessageType.NOTICE,
            channelName = "Status"
        )

        _conversations.value = _conversations.value.map {
            if (it.id == statusConversation.id) {
                it.copy(messages = (it.messages + systemMessage).toMutableList())
            } else {
                it
            }
        }
    }
    fun addResponseTo(message: String, target: String) {
        val conversation = _conversations.value.find { it.name == target }
            ?: _conversations.value.find { it.type == ConversationType.SERVER_STATUS }
            ?: return

        val responseMessage = IRCMessage(
            sender = "System",
            content = message,
            conversationType = conversation.type,
            type = MessageType.NOTICE,
            channelName = target
        )

        // Actualizar la conversación correspondiente
        _conversations.value = _conversations.value.map {
            if (it.id == conversation.id) {
                it.copy(messages = (it.messages + responseMessage).toMutableList())
            } else {
                it
            }
        }
    }


    // Funciones de modificación
    fun setUseColors(enable: Boolean) {
        viewModelScope.launch {
            ircColorPreferences.setUseColors(enable)
        }
    }

    fun setDefaultColor(colorCode: Int) {
        viewModelScope.launch {
            ircColorPreferences.setDefaultColor(colorCode)
        }
    }
    // Función ÚNICA para formatear mensajes
    fun formatMessage(content: String): AnnotatedString {
        return if (ircColorPreferences.useColors.value) {
            IRCMessageFormatter().formatMessage(content)
        } else {
            buildAnnotatedString { append(content) }
        }
    }
    fun setBackgroundColor(colorCode: Int) {
        viewModelScope.launch {
            ircColorPreferences.setBackgroundColor(colorCode)
        }
    }
    fun getDisableColorCodes(): StateFlow<Boolean> {
        return ircColorPreferences.getDisableColorCodes()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
    }

    suspend fun setDisableColorCodes(disable: Boolean) {
        ircColorPreferences.setDisableColorCodes(disable)
    }

    // 3. Opcional: Función para resetear el color de fondo al valor por defecto
    fun resetBackgroundColor() {
        viewModelScope.launch {
            ircColorPreferences.setBackgroundColor(0) // 0 = usar tema del sistema
        }
    }
    // Nuevos métodos
    fun setShowJoinEvents(show: Boolean) {
        viewModelScope.launch {
            chatPreferences.setShowJoinEvents(show)
        }
    }

    fun setShowQuitEvents(show: Boolean) {
        viewModelScope.launch {
            chatPreferences.setShowQuitEvents(show)
        }
    }

    fun setShowPartEvents(show: Boolean) {
        viewModelScope.launch {
            chatPreferences.setShowPartEvents(show)
        }
    }

    fun setShowBanEvents(show: Boolean) {
        viewModelScope.launch {
            chatPreferences.setShowBanEvents(show)
        }
    }

    fun setFontSize(size: Int) {
        viewModelScope.launch {
            chatPreferences.setFontSize(size)
        }
    }
    fun setCompactMessageFormat(enabled: Boolean) {
        viewModelScope.launch {
            chatPreferences.setCompactMessageFormat(enabled)
        }
    }

}