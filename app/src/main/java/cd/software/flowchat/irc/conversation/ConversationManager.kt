
package cd.software.flowchat.irc.conversation

import android.util.Log
import cd.software.flowchat.model.Channel
import cd.software.flowchat.model.ChannelUser
import cd.software.flowchat.model.Conversation
import cd.software.flowchat.model.ConversationType
import cd.software.flowchat.model.IRCMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.pircbotx.PircBotX

/**
 * Gestiona todas las conversaciones (canales, mensajes privados, estado del servidor).
 * Responsable de crear, actualizar y eliminar conversaciones, así como de agregar mensajes.
 */
class ConversationManager {
    
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()
    
    private val _channelRemovalConfirmed = MutableStateFlow<String?>(null)
    val channelRemovalConfirmed: StateFlow<String?> = _channelRemovalConfirmed.asStateFlow()
    
    private val _availableChannels = MutableStateFlow<List<Channel>>(emptyList())
    val availableChannels: StateFlow<List<Channel>> = _availableChannels.asStateFlow()
    
    /**
     * Agrega un mensaje a la conversación correspondiente.
     */
    fun addMessage(message: IRCMessage) {
        val updatedConversations = _conversations.value.map { conversation ->
            if (conversation.name == message.channelName) {
                val updatedMessages = conversation.messages.toMutableList().apply {
                    add(message)
                }
                conversation.copy(messages = updatedMessages)
            } else {
                conversation
            }
        }
        
        if (_conversations.value != updatedConversations) {
            _conversations.value = updatedConversations
        }
    }
    
    /**
     * Obtiene o crea la conversación de estado del servidor (Status).
     */
    fun getOrCreateStatusConversation(): Conversation {
        val existingStatus = _conversations.value.find {
            it.type == ConversationType.SERVER_STATUS
        }
        
        return existingStatus ?: run {
            val newStatus = Conversation(
                name = "Status",
                type = ConversationType.SERVER_STATUS,
                origin = ConversationType.SERVER_STATUS,
                messages = mutableListOf()
            )
            
            _conversations.value = listOf(newStatus) + _conversations.value.filter {
                it.type != ConversationType.SERVER_STATUS
            }
            
            newStatus
        }
    }
    
    /**
     * Inicia o recupera una conversación privada con un usuario.
     */
    fun startPrivateConversation(nickname: String): Conversation {
        val existingConversation = _conversations.value.find {
            it.name == nickname && it.type == ConversationType.PRIVATE_MESSAGE
        }
        
        return existingConversation ?: run {
            val newConversation = Conversation(
                name = nickname,
                type = ConversationType.PRIVATE_MESSAGE,
                origin = ConversationType.PRIVATE_MESSAGE
            )
            _conversations.value = _conversations.value + newConversation
            newConversation
        }
    }
    
    /**
     * Inicia una conversación con un servicio IRC (NickServ, ChanServ, etc.).
     */
    fun startServiceConversation(serviceName: String) {
        val existing = _conversations.value.find {
            it.name.equals(serviceName, true) && it.type == ConversationType.SERVICE
        }
        
        if (existing == null) {
            _conversations.value += Conversation(
                name = serviceName,
                type = ConversationType.SERVICE,
                origin = ConversationType.SERVICE
            )
        }
    }
    
    /**
     * Maneja la creación automática de conversación cuando se une a un canal.
     */
    fun handleAutoJoinChannel(channelName: String) {
        val existingConversation = _conversations.value.find { it.name == channelName }
        
        if (existingConversation == null) {
            val newConversation = Conversation(
                name = channelName,
                type = ConversationType.CHANNEL,
                origin = ConversationType.CHANNEL,
                messages = mutableListOf()
            )
            _conversations.value = _conversations.value + newConversation
        }
    }
    
    /**
     * Elimina un canal de las conversaciones.
     */
    fun removeChannel(channelName: String) {
        val updatedConversations = _conversations.value.filter { it.name != channelName }
        _conversations.value = updatedConversations
    }
    
    /**
     * Elimina una conversación privada.
     */
    fun removePrivateConversation(nickname: String) {
        val updatedConversations = _conversations.value.filter {
            !(it.type == ConversationType.PRIVATE_MESSAGE && it.name == nickname)
        }
        _conversations.value = updatedConversations
    }
    
    /**
     * Limpia todas las conversaciones excepto la de estado del servidor.
     */
    fun clearAllExceptStatus() {
        _conversations.value = _conversations.value.filter { 
            it.type == ConversationType.SERVER_STATUS 
        }
    }
    
    /**
     * Confirma la eliminación de un canal.
     */
    fun confirmChannelRemoval(channelName: String) {
        _channelRemovalConfirmed.value = channelName
    }
    
    /**
     * Obtiene la lista de usuarios de un canal específico.
     */
    fun getUsersForChannel(channelName: String, bot: PircBotX?): List<ChannelUser> {
        return try {
            val channel = bot?.getUserChannelDao()?.getChannel(channelName) ?: return emptyList()
            
            channel.users.map { user ->
                ChannelUser(
                    nickname = user.nick,
                    isOp = channel.isOp(user),
                    isOwner = channel.isOwner(user),
                    isVoice = channel.hasVoice(user),
                    isHalfOp = channel.isHalfOp(user),
                    isAdmin = channel.isSuperOp(user)
                )
            }.sortedWith(compareByDescending<ChannelUser> { it.getRankPriority() }
                .thenBy { it.nickname })
        } catch (e: org.pircbotx.exception.DaoException) {
            Log.e("ConversationManager", "Error al obtener usuarios para canal $channelName: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Actualiza la lista de canales disponibles en el servidor.
     */
    fun updateAvailableChannels(channels: List<Channel>) {
        _availableChannels.value = channels
    }
    
    /**
     * Limpia la lista de canales disponibles.
     */
    fun clearAvailableChannels() {
        _availableChannels.value = emptyList()
    }
}
