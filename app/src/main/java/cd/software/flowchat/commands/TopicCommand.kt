package cd.software.flowchat.commands

import android.content.Context
import cd.software.flowchat.IRCService
import es.chat.R
import cd.software.flowchat.model.Conversation
import cd.software.flowchat.model.ConversationType
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class TopicCommand(context: Context) : BaseCommand(context) {
    override val name = "topic"
    override val aliases = listOf("t")
    override val description = context.getString(R.string.topic_command_description)
    override val usage = context.getString(R.string.topic_command_usage)
    override val availableInChannel = true
    override val availableInPrivate = false
    override val requiresConnection = true

    override suspend fun execute(
        service: IRCService,
        conversation: Conversation,
        args: List<String>,
        rawMessage: String
    ): CommandResult = suspendCoroutine { continuation ->
        if (conversation.type != ConversationType.CHANNEL) {
            continuation.resume(error(context.getString(R.string.topic_error_channel_only)))
            return@suspendCoroutine
        }

        val channelName = conversation.name

        // Si no hay argumentos, mostrar el topic actual
        if (args.isEmpty()) {
            try {
                val channel = service.bot?.getUserChannelDao()?.getChannel(channelName)
                val currentTopic = channel?.topic ?: ""

                if (currentTopic.isNotEmpty()) {
                    continuation.resume(success(context.getString(R.string.topic_current_topic, currentTopic)))
                } else {
                    continuation.resume(success(context.getString(R.string.topic_no_topic_set)))
                }
            } catch (e: Exception) {
                continuation.resume(error(context.getString(R.string.topic_error_get_topic, e.message ?: "")))
            }
            return@suspendCoroutine
        }

        // Cambiar el topic
        val newTopic = args.joinToString(" ")

        service.setTopic(channelName, newTopic) { success, errorMessage ->
            if (success) {
                continuation.resume(success(context.getString(R.string.topic_success_change_request, newTopic)))
            } else {
                continuation.resume(error(errorMessage ?: context.getString(R.string.topic_error_unknown)))
            }
        }
    }
}
