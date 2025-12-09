package cd.software.flowchat.irc.commands

import android.util.Log
import cd.software.flowchat.irc.connection.IRCConnectionManager
import cd.software.flowchat.irc.conversation.ConversationManager
import cd.software.flowchat.model.Conversation
import cd.software.flowchat.model.ConversationType
import cd.software.flowchat.model.EventColorType
import cd.software.flowchat.model.IRCMessage
import cd.software.flowchat.model.MessageEventType
import cd.software.flowchat.model.MessageType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Ejecuta comandos IRC. Responsable de enviar comandos al servidor y actualizar el estado local.
 */
class IRCCommandExecutor(
        private val connectionManager: IRCConnectionManager,
        private val conversationManager: ConversationManager,
        private val serviceScope: CoroutineScope
) {

    // ========== Operaciones de Canal ==========

    /** Une a un canal. */
    fun joinChannel(channel: String) {
        serviceScope.launch {
            try {
                val currentBot = connectionManager.bot
                if (currentBot == null || !currentBot.isConnected) {
                    throw IllegalStateException("Bot is not connected or null")
                }

                if (channel.isBlank()) {
                    throw IllegalArgumentException("Channel name is empty")
                }

                withContext(Dispatchers.IO) { currentBot.send()?.joinChannel(channel) }

                // Crear conversación solo si no existe
                val existingConversation =
                        conversationManager.conversations.value.find { it.name == channel }
                if (existingConversation == null) {
                    conversationManager.handleAutoJoinChannel(channel)
                }
            } catch (e: Exception) {
                Log.e("IRCCommandExecutor", "Failed to join channel: ${e.message}")
            }
        }
    }

    /** Sale de un canal. */
    fun partChannel(channelName: String) {
        serviceScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    connectionManager.bot?.sendRaw()?.rawLine("PART $channelName")
                }

                val leaveEvent =
                        IRCMessage(
                                sender = "",
                                content = "You have left the channel $channelName",
                                conversationType = ConversationType.CHANNEL,
                                type = MessageType.TEXT,
                                eventType = MessageEventType.USER_PART,
                                channelName = channelName
                        )
                conversationManager.addMessage(leaveEvent)
            } catch (e: Exception) {
                Log.e("IRCCommandExecutor", "Failed to part channel: ${e.message}")
            }
        }
    }

    /** Establece el topic de un canal. */
    fun setTopic(channel: String, topic: String, onResult: (Boolean, String?) -> Unit) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                val channelObj = connectionManager.bot?.getUserChannelDao()?.getChannel(channel)
                channelObj?.send()?.setTopic(topic)

                withContext(Dispatchers.Main) {
                    onResult(true, null)

                    conversationManager.addMessage(
                            IRCMessage(
                                    sender = connectionManager.getBotNick() ?: "Me",
                                    content = "Tema cambiado a: \"$topic\"",
                                    conversationType = ConversationType.CHANNEL,
                                    type = MessageType.TEXT,
                                    eventType = MessageEventType.TOPIC_CHANGE,
                                    channelName = channel,
                                    eventColorType = EventColorType.TOPIC_CHANGE
                            )
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false, e.message ?: "Error desconocido al cambiar el tema")

                    conversationManager.addMessage(
                            IRCMessage(
                                    sender = "System",
                                    content = "Error al cambiar el tema: ${e.message}",
                                    conversationType = ConversationType.CHANNEL,
                                    type = MessageType.TEXT,
                                    eventType = MessageEventType.ERROR,
                                    channelName = channel,
                                    eventColorType = EventColorType.ERROR
                            )
                    )
                }
                Log.e("IRCCommandExecutor", "Error setting topic", e)
            }
        }
    }

    /** Solicita la lista de canales disponibles. */
    fun fetchAvailableChannels() {
        serviceScope.launch(Dispatchers.IO) {
            try {
                connectionManager.bot?.send()?.listChannels()
            } catch (e: Exception) {
                conversationManager.clearAvailableChannels()
                Log.e("IRCCommandExecutor", "Error fetching available channels", e)
            }
        }
    }

    /** Verifica si un canal existe. */
    fun channelExists(channelName: String): Boolean {
        return try {
            connectionManager.bot?.userChannelDao?.getChannel(channelName)
            true
        } catch (e: org.pircbotx.exception.DaoException) {
            Log.d("IRCCommandExecutor", "Canal no encontrado: $channelName")
            false
        }
    }

    // ========== Operaciones de Mensajes ==========

    /** Envía un mensaje a una conversación. */
    fun sendMessage(conversation: Conversation, message: String) {
        serviceScope.launch {
            try {
                val currentBot = connectionManager.bot
                if (currentBot == null || !currentBot.isConnected) {
                    throw IllegalStateException("Bot is not connected")
                }

                withContext(Dispatchers.IO) {
                    if (conversation.type == ConversationType.PRIVATE_MESSAGE &&
                                    conversation.name.equals("NickServ", ignoreCase = true)
                    ) {
                        currentBot.sendRaw().rawLine("PRIVMSG ${conversation.name} :$message")
                    } else {
                        when (conversation.type) {
                            ConversationType.CHANNEL -> {
                                currentBot
                                        .sendRaw()
                                        .rawLine("PRIVMSG ${conversation.name} :$message")
                            }
                            ConversationType.PRIVATE_MESSAGE -> {
                                currentBot
                                        .sendRaw()
                                        .rawLine("PRIVMSG ${conversation.name} :$message")
                            }
                            else ->
                                    throw UnsupportedOperationException(
                                            "Unsupported conversation type"
                                    )
                        }
                    }
                }

                conversationManager.addMessage(
                        IRCMessage(
                                sender = currentBot.nick ?: "Me",
                                content = message,
                                conversationType = conversation.type,
                                type = MessageType.TEXT,
                                isOwnMessage = true,
                                channelName = conversation.name
                        )
                )
            } catch (e: Exception) {
                Log.e("IRCCommandExecutor", "Failed to send message: ${e.message}")
            }
        }
    }

    /** Envía un mensaje privado. */
    fun sendPrivateMessage(nickname: String, message: String) {
        conversationManager.startPrivateConversation(nickname)

        val privateConversation =
                Conversation(name = nickname, type = ConversationType.PRIVATE_MESSAGE)
        sendMessage(privateConversation, message)
    }

    /** Envía un notice. */
    fun sendNotice(target: String, message: String, currentChannelName: String? = null) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                connectionManager.bot?.sendIRC()?.notice(target, message)

                if (currentChannelName != null) {
                    conversationManager.addMessage(
                            IRCMessage(
                                    sender = connectionManager.getBotNick() ?: "Me",
                                    content = "[→ $target] $message",
                                    conversationType = ConversationType.CHANNEL,
                                    type = MessageType.NOTICE,
                                    isOwnMessage = true,
                                    channelName = currentChannelName,
                                    eventColorType = EventColorType.NOTICE
                            )
                    )
                } else {
                    val isChannel = target.startsWith("#") || target.startsWith("&")
                    val conversationType =
                            if (isChannel) ConversationType.CHANNEL
                            else ConversationType.PRIVATE_MESSAGE

                    if (!isChannel) {
                        conversationManager.startPrivateConversation(target)
                    }

                    conversationManager.addMessage(
                            IRCMessage(
                                    sender = connectionManager.getBotNick() ?: "Me",
                                    content = message,
                                    conversationType = conversationType,
                                    type = MessageType.NOTICE,
                                    isOwnMessage = true,
                                    channelName = target,
                                    eventColorType = EventColorType.NOTICE
                            )
                    )
                }
            } catch (e: Exception) {
                Log.e("IRCCommandExecutor", "Error sending notice", e)
            }
        }
    }

    /** Envía una acción (/me). */
    fun sendAction(target: String, action: String) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                connectionManager.bot?.sendIRC()?.action(target, action)
            } catch (e: Exception) {
                Log.e("IRCCommandExecutor", "Error sending action", e)
            }
        }
    }

    // ========== Operaciones de Usuario ==========

    /** Cambia el nickname. */
    fun changeNick(newNick: String): Boolean {
        return try {
            serviceScope.launch(Dispatchers.IO) {
                connectionManager.bot?.sendIRC()?.changeNick(newNick)
            }
            true
        } catch (e: Exception) {
            Log.e("IRCCommandExecutor", "Error changing nick", e)
            false
        }
    }

    /** Solicita información WHOIS de un usuario. */
    fun requestWhois(nickname: String) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                if (nickname.isBlank()) {
                    throw IllegalArgumentException("El nickname no puede estar vacío")
                }

                if (connectionManager.bot == null || !connectionManager.bot!!.isConnected) {
                    throw IllegalStateException("No hay conexión activa con el servidor")
                }

                connectionManager.bot?.sendRaw()?.rawLine("WHOIS $nickname")

                conversationManager.addMessage(
                        IRCMessage(
                                sender = "Sistema",
                                content = "Solicitando información WHOIS para $nickname...",
                                conversationType = ConversationType.SERVER_STATUS,
                                type = MessageType.TEXT,
                                eventType = MessageEventType.WHOIS_RESPONSE,
                                channelName = "Status",
                                eventColorType = EventColorType.SYSTEM_INFO
                        )
                )
            } catch (e: Exception) {
                Log.e("IRCCommandExecutor", "Error al ejecutar comando WHOIS", e)

                conversationManager.addMessage(
                        IRCMessage(
                                sender = "Sistema",
                                content = "Error al solicitar WHOIS: ${e.message}",
                                conversationType = ConversationType.SERVER_STATUS,
                                type = MessageType.TEXT,
                                eventType = MessageEventType.ERROR,
                                channelName = "Status",
                                eventColorType = EventColorType.ERROR
                        )
                )
            }
        }
    }

    // ========== Operaciones de Moderación ==========

    /** Da operador a un usuario. */
    fun opUser(channel: String, nickname: String) {
        serviceScope.launch(Dispatchers.IO) {

            try {
                connectionManager.bot?.sendIRC()?.mode(channel, "+o $nickname")
            } catch (e: Exception) {
                Log.e("IRCCommandExecutor", "Error giving op", e)
            }
        }
    }

    /** Quita operador a un usuario. */
    fun deopUser(channel: String, nickname: String) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                connectionManager.bot?.sendIRC()?.mode(channel, "-o $nickname")
            } catch (e: Exception) {
                Log.e("IRCCommandExecutor", "Error removing op", e)
            }
        }
    }

    /** Da voice a un usuario. */
    fun voiceUser(channel: String, nickname: String) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                connectionManager.bot?.sendIRC()?.mode(channel, "+v $nickname")
            } catch (e: Exception) {
                Log.e("IRCCommandExecutor", "Error giving voice", e)
            }
        }
    }

    /** Quita voice a un usuario. */
    fun devoiceUser(channel: String, nickname: String) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                connectionManager.bot?.sendIRC()?.mode(channel, "-v $nickname")
            } catch (e: Exception) {
                Log.e("IRCCommandExecutor", "Error removing voice", e)
            }
        }
    }

    /** Banea a un usuario. */
    fun banUser(channel: String, hostmask: String) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                connectionManager.bot?.sendIRC()?.mode(channel, "+b $hostmask")
            } catch (e: Exception) {
                Log.e("IRCCommandExecutor", "Error banning user", e)
            }
        }
    }

    /** Desbanea a un usuario. */
    fun unbanUser(channel: String, hostmask: String) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                connectionManager.bot?.sendIRC()?.mode(channel, "-b $hostmask")
            } catch (e: Exception) {
                Log.e("IRCCommandExecutor", "Error unbanning user", e)
            }
        }
    }

    /** Invita a un usuario a un canal. */
    fun inviteUser(nickname: String, channel: String) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                connectionManager.bot?.sendIRC()?.invite(nickname, channel)
            } catch (e: Exception) {
                Log.e("IRCCommandExecutor", "Error inviting user", e)
            }
        }
    }

    /** Establece un modo en un canal. */
    fun setMode(channel: String, mode: String) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                connectionManager.bot?.sendIRC()?.mode(channel, mode)
            } catch (e: Exception) {
                Log.e("IRCCommandExecutor", "Error setting mode", e)
            }
        }
    }

    /** Solicita la lista de bans de un canal. */
    fun requestBanList(channel: String) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                connectionManager.bot?.sendRaw()?.rawLine("MODE $channel +b")
            } catch (e: Exception) {
                Log.e("IRCCommandExecutor", "Error requesting ban list", e)
            }
        }
    }

    // ========== Operaciones de Servicio ==========

    /** Envía un comando a un servicio IRC. */
    fun sendServiceCommand(service: String, command: String) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                connectionManager.bot?.sendIRC()?.message(service, command)
            } catch (e: Exception) {
                Log.e("IRCCommandExecutor", "Error sending service command", e)
            }
        }
    }

    /** Envía un comando a NickServ. */
    fun sendNickServCommand(command: String) {
        sendServiceCommand("NickServ", command)
    }

    /** Envía un comando a ChanServ. */
    fun sendChanServCommand(command: String) {
        sendServiceCommand("Operserv", command)
    }

    // ========== Operaciones Raw ==========

    /** Envía un comando raw al servidor. */
    fun sendRawCommand(command: String) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                connectionManager.bot?.sendRaw()?.rawLine(command)
            } catch (e: Exception) {
                Log.e("IRCCommandExecutor", "Error sending raw command", e)
            }
        }
    }

    /** Ejecuta un comando del servidor. */
    fun executeServerCommand(command: String, arguments: String = "") {
        serviceScope.launch(Dispatchers.IO) {
            try {
                if (command.isEmpty()) {
                    connectionManager.bot?.sendRaw()?.rawLine(arguments)
                } else {
                    connectionManager.bot?.sendRaw()?.rawLine("$command $arguments")
                }
            } catch (e: Exception) {
                Log.e("IRCCommandExecutor", "Error executing server command", e)
                throw e
            }
        }
    }

    /** Ejecuta un comando raw completo. */
    fun executeServerCommand(rawCommand: String) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                connectionManager.bot?.sendRaw()?.rawLine(rawCommand)
            } catch (e: Exception) {
                Log.e("IRCCommandExecutor", "Error executing server command", e)
                throw e
            }
        }
    }

    /** Envía un whisper (si el servidor lo soporta). */
    fun whisper(target: String, message: String) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                connectionManager
                        .bot
                        ?.sendRaw()
                        ?.rawLine("PRIVMSG $target :\u0001WHISPER $message\u0001")
            } catch (e: Exception) {
                Log.e("IRCCommandExecutor", "Error sending whisper", e)
            }
        }
    }
}
