package cd.software.flowchat.mircolors.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cd.software.flowchat.mircolors.IRCColors
import cd.software.flowchat.mircolors.IRCMessageFormatter

/**
 * Editor de mensajes con soporte para colores
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColoredMessageEditor(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    modifier: Modifier = Modifier,
    enableColorFormatting: Boolean = true,
    onToggleColorSelector: () -> Unit,
    isColorSelectorVisible: Boolean = false,
    selectedColor: Int = -1,
    onColorSelected: (Int) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        // Selector de colores (visible solo cuando se activa)
        AnimatedVisibility(visible = isColorSelectorVisible && enableColorFormatting) {
            IRCColorSelector(
                onColorSelected = onColorSelected,
                selectedColor = selectedColor
            )
        }

        // Editor de texto y botones
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón de formato
            if (enableColorFormatting) {
                IconButton(
                    onClick = onToggleColorSelector,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                if (selectedColor >= 0)
                                    IRCColors.getColorByCode(selectedColor)
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                    )
                }
            }

            // Campo de texto
            TextField(
                value = messageText,
                onValueChange = onMessageTextChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                placeholder = { Text("Escribe un mensaje...") },
                singleLine = false,
                maxLines = 4
            )

            // Botón de enviar
            IconButton(
                onClick = onSendMessage,
                enabled = messageText.isNotBlank()
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Send,
                    contentDescription = "Enviar mensaje"
                )
            }
        }
    }
}

/**
 * Previsualización de mensaje con colores
 */
@Composable
fun ColoredMessagePreview(
    messageText: String,
    formatter: IRCMessageFormatter = remember { IRCMessageFormatter() },
    modifier: Modifier = Modifier
) {
    val formattedText = remember(messageText) {
        formatter.formatMessage(messageText)
    }

    if (messageText.isNotEmpty()) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "Previsualización:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = formattedText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Botones para insertar formatos rápidos
 */
@Composable
fun FormattingToolbar(
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onUnderlineClick: () -> Unit,
    onColorClick: () -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FormattingButton(
            text = "B",
            onClick = onBoldClick,
            isBold = true
        )

        FormattingButton(
            text = "I",
            onClick = onItalicClick,
            isItalic = true
        )

        FormattingButton(
            text = "U",
            onClick = onUnderlineClick,
            isUnderlined = true
        )

        FormattingButton(
            text = "Color",
            onClick = onColorClick
        )

        FormattingButton(
            text = "Reset",
            onClick = onResetClick
        )
    }
}

/**
 * Botón individual para la barra de formato
 */
@Composable
private fun FormattingButton(
    text: String,
    onClick: () -> Unit,
    isBold: Boolean = false,
    isItalic: Boolean = false,
    isUnderlined: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(36.dp),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        Text(
            text = text,
            fontWeight = if (isBold) androidx.compose.ui.text.font.FontWeight.Bold else null,
            fontStyle = if (isItalic) androidx.compose.ui.text.font.FontStyle.Italic else null,
            textDecoration = if (isUnderlined) androidx.compose.ui.text.style.TextDecoration.Underline else null
        )
    }
}