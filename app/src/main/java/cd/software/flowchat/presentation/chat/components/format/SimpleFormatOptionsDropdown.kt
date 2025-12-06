package cd.software.flowchat.presentation.chat.components.format

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cd.software.flowchat.mircolors.IRCColors
import es.chat.R


/**
 * Dropdown simplificado de opciones de formato (sin colores) Versión ligera para cuando no se
 * necesita selector de colores
 */
@Composable
fun SimpleFormatOptionsDropdown(
        showFormatOptions: Boolean,
        onDismiss: () -> Unit,
        onInsertFormatting: (Char) -> Unit,
        onInsertReset: () -> Unit,
        onResetAll: () -> Unit,
        isCompact: Boolean = false,
        modifier: Modifier = Modifier
) {
    val dropdownWidth = if (isCompact) 260.dp else 300.dp
    val contentPadding = if (isCompact) 6.dp else 8.dp
    val sectionSpacing = if (isCompact) 6.dp else 8.dp

    DropdownMenu(
            expanded = showFormatOptions,
            onDismissRequest = onDismiss,
            modifier = modifier.width(dropdownWidth)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(contentPadding)) {
            // Text formatting section
            Text(
                    text = stringResource(R.string.text_formatting_title),
                    style =
                            if (isCompact) MaterialTheme.typography.labelSmall
                            else MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier =
                            Modifier.padding(
                                    horizontal = 4.dp,
                                    vertical = if (isCompact) 2.dp else 4.dp
                            )
            )

            // Botones de formato básico
            if (isCompact) {
                Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CompactFormatButton(
                                icon = Icons.Default.Star,
                                onClick = {
                                    onInsertFormatting(IRCColors.BOLD_CHAR)
                                    onDismiss()
                                },
                                text = stringResource(R.string.format_bold),
                                iconSize = 14.dp
                        )
                        CompactFormatButton(
                                icon = Icons.Default.KeyboardArrowDown,
                                onClick = {
                                    onInsertFormatting(IRCColors.ITALIC_CHAR)
                                    onDismiss()
                                },
                                text = stringResource(R.string.format_italic),
                                iconSize = 14.dp
                        )
                    }
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CompactFormatButton(
                                icon = Icons.Default.PlayArrow,
                                onClick = {
                                    onInsertFormatting(IRCColors.UNDERLINE_CHAR)
                                    onDismiss()
                                },
                                text = stringResource(R.string.format_underline),
                                iconSize = 14.dp
                        )
                        CompactFormatButton(
                                icon = Icons.Default.Clear,
                                onClick = {
                                    onInsertReset()
                                    onDismiss()
                                },
                                text = stringResource(R.string.format_reset),
                                iconSize = 14.dp
                        )
                    }
                }
            } else {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FormatButton(
                            icon = Icons.Default.Star,
                            onClick = {
                                onInsertFormatting(IRCColors.BOLD_CHAR)
                                onDismiss()
                            },
                            text = stringResource(R.string.format_bold)
                    )
                    FormatButton(
                            icon = Icons.Default.KeyboardArrowDown,
                            onClick = {
                                onInsertFormatting(IRCColors.ITALIC_CHAR)
                                onDismiss()
                            },
                            text = stringResource(R.string.format_italic)
                    )
                    FormatButton(
                            icon = Icons.Default.PlayArrow,
                            onClick = {
                                onInsertFormatting(IRCColors.UNDERLINE_CHAR)
                                onDismiss()
                            },
                            text = stringResource(R.string.format_underline)
                    )
                    FormatButton(
                            icon = Icons.Default.Clear,
                            onClick = {
                                onInsertReset()
                                onDismiss()
                            },
                            text = stringResource(R.string.format_reset)
                    )
                }
            }

            Spacer(modifier = Modifier.height(sectionSpacing))

            // Reset all button
            TextButton(
                    onClick = {
                        onResetAll()
                        onDismiss()
                    },
                    modifier = Modifier.align(Alignment.End),
                    contentPadding =
                            PaddingValues(
                                    horizontal = if (isCompact) 8.dp else 12.dp,
                                    vertical = if (isCompact) 4.dp else 6.dp
                            )
            ) {
                Text(
                        stringResource(R.string.format_clear_all),
                        style =
                                if (isCompact) MaterialTheme.typography.bodySmall
                                else MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
