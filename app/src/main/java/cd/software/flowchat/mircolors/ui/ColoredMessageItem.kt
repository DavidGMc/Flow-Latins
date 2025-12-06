package cd.software.flowchat.mircolors.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cd.software.flowchat.mircolors.IRCMessageFormatter
import cd.software.flowchat.model.IRCMessage

/**
 * Componente que muestra un mensaje IRC con soporte para colores
 */
@Composable
fun ColoredMessageItem(
    message: IRCMessage,
    modifier: Modifier = Modifier,
    showTimestamp: Boolean = true,
    formatter: IRCMessageFormatter = remember { IRCMessageFormatter() }
) {
    val formattedContent = remember(message.content) {
        formatter.formatMessage(message.content)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Avatar o indicador de usuario (opcional)
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message.sender.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Contenido del mensaje
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Nombre del remitente y timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message.sender,
                    style = MaterialTheme.typography.titleSmall
                )

                if (showTimestamp) {
                    Text(
                        text = message.timestamp?.toString() ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Contenido con colores
            Text(
                text = formattedContent,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
