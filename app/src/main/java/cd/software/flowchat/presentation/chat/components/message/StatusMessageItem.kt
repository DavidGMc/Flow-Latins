package cd.software.flowchat.presentation.chat.components.message

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cd.software.flowchat.mircolors.IRCMessageFormatter
import cd.software.flowchat.model.IRCMessage
import cd.software.flowchat.presentation.chat.components.common.FormattedClickableText
import cd.software.flowchat.presentation.chat.utils.copyToClipboard
import cd.software.flowchat.presentation.chat.utils.extractUrls
import cd.software.flowchat.presentation.chat.utils.formatTimestamp
import cd.software.flowchat.utils.showToast
import cd.software.flowchat.viewmodel.IRCChatViewModel
import es.chat.R

/**
 * Componente para mostrar mensajes de estado del sistema
 * Muestra mensajes de eventos del servidor (joins, parts, quits, etc.)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StatusMessageItem(
    message: IRCMessage,
    chatViewModel: IRCChatViewModel,
    context: Context,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val fontSize by chatViewModel.fontSize.collectAsState()
    val formatter = remember { IRCMessageFormatter() }

    // 1. Formatear el mensaje (sanitizado y con colores IRC)
    val formattedContent = remember(message.content) {
        formatter.formatMessage(message.content)
    }

    // 2. Obtener los rangos de las URLs para hacerlas clickeables
    val urlRanges = remember(message.content) { message.content.extractUrls() }

    // Sistema de colores para nicknames
    val nickColors = listOf(
        Color(0xFF3498DB), Color(0xFFE74C3C), Color(0xFF2ECC71),
        Color(0xFFF39C12), Color(0xFF9B59B6), Color(0xFF1ABC9C),
        MaterialTheme.colorScheme.primary,
    )
    val nickColor = nickColors[kotlin.math.abs(message.sender.hashCode()) % nickColors.size]

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = {},
                onLongClick = { context.copyToClipboard(message.content) }
            )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header (nick + timestamp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = message.sender,
                    style = MaterialTheme.typography.bodySmall,
                    color = nickColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize.sp
                )

                Text(
                    text = formatTimestamp(message.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurface.copy(alpha = 0.5f),
                    fontSize = fontSize.sp
                )
            }

            // Contenido principal con formato IRC y URLs clickeables
            FormattedClickableText(
                message = message.content,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = colorScheme.onSurface,
                    fontSize = fontSize.sp
                ),
                modifier = Modifier.padding(top = 2.dp),
                onClickUrl = { url ->
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        context.showToast(R.string.url_open_error)
                    }
                }
            )

            // Previews de URLs
            message.content.extractUrls().takeIf { it.isNotEmpty() }?.let { urls ->
                Spacer(modifier = Modifier.height(8.dp))
                Divider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = colorScheme.onSurface.copy(alpha = 0.1f),
                    thickness = 0.5.dp
                )
                Column {
                    urls.forEach { urlRange ->
                        val url = message.content.substring(urlRange.first, urlRange.second)
                        UrlPreviewCard(
                            url = url,
                            context = context,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
