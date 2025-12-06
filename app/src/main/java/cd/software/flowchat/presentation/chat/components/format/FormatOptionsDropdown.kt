package cd.software.flowchat.presentation.chat.components.format

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cd.software.flowchat.mircolors.IRCColors
import cd.software.flowchat.mircolors.ui.IRCColorSelector
import es.chat.R


/**
 * Dropdown completo de opciones de formato con selector de colores Incluye formato de texto
 * (negrita, cursiva, subrayado) y selector de colores IRC
 */
@Composable
fun FormatOptionsDropdown(
        showFormatOptions: Boolean,
        onDismiss: () -> Unit,
        defaultColor: Int,
        currentBgColor: Int,
        onInsertFormatting: (Char) -> Unit,
        onInsertReset: () -> Unit,
        onResetAll: () -> Unit,
        onApplyColor: (Int) -> Unit,
        onApplyBackground: (Int) -> Unit,
        onClearBackground: () -> Unit,
        showForegroundColorSelector: Boolean,
        showBackgroundColorSelector: Boolean,
        onForegroundSelected: () -> Unit,
        onBackgroundSelected: () -> Unit,
        disableColorCodes: Boolean,
        isCompact: Boolean = false,
        modifier: Modifier = Modifier
) {
    // Configuración responsive basada en isCompact
    val dropdownWidth = if (isCompact) 260.dp else 300.dp
    val contentPadding = if (isCompact) 6.dp else 8.dp
    val iconSize = if (isCompact) 14.dp else 16.dp
    val dividerPadding = if (isCompact) 6.dp else 8.dp
    val sectionSpacing = if (isCompact) 6.dp else 8.dp
    val buttonSpacing = if (isCompact) 4.dp else 8.dp

    DropdownMenu(
            expanded = showFormatOptions,
            onDismissRequest = onDismiss,
            modifier = modifier.width(dropdownWidth)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(contentPadding)) {
            // Text formatting section
            DropdownSectionHeader(
                    text = stringResource(R.string.text_formatting_title),
                    isCompact = isCompact
            )

            // Botones de formato - Layout adaptativo
            if (isCompact) {
                // Layout vertical más compacto para pantallas pequeñas
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
                                onClick = { onInsertFormatting(IRCColors.BOLD_CHAR) },
                                text = stringResource(R.string.format_bold),
                                iconSize = iconSize
                        )
                        CompactFormatButton(
                                icon = Icons.Default.KeyboardArrowDown,
                                onClick = { onInsertFormatting(IRCColors.ITALIC_CHAR) },
                                text = stringResource(R.string.format_italic),
                                iconSize = iconSize
                        )
                    }
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CompactFormatButton(
                                icon = Icons.Default.PlayArrow,
                                onClick = { onInsertFormatting(IRCColors.UNDERLINE_CHAR) },
                                text = stringResource(R.string.format_underline),
                                iconSize = iconSize
                        )
                        CompactFormatButton(
                                icon = Icons.Default.Clear,
                                onClick = { onInsertReset() },
                                text = stringResource(R.string.format_reset),
                                iconSize = iconSize
                        )
                    }
                }
            } else {
                // Layout horizontal original para pantallas normales
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FormatButton(
                            icon = Icons.Default.Star,
                            onClick = { onInsertFormatting(IRCColors.BOLD_CHAR) },
                            text = stringResource(R.string.format_bold)
                    )
                    FormatButton(
                            icon = Icons.Default.KeyboardArrowDown,
                            onClick = { onInsertFormatting(IRCColors.ITALIC_CHAR) },
                            text = stringResource(R.string.format_italic)
                    )
                    FormatButton(
                            icon = Icons.Default.PlayArrow,
                            onClick = { onInsertFormatting(IRCColors.UNDERLINE_CHAR) },
                            text = stringResource(R.string.format_underline)
                    )
                    FormatButton(
                            icon = Icons.Default.Clear,
                            onClick = { onInsertReset() },
                            text = stringResource(R.string.format_reset)
                    )
                }
            }

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

            if (!disableColorCodes) {
                Divider(modifier = Modifier.padding(vertical = dividerPadding))

                // Color selection tabs - Diseño adaptativo
                if (isCompact) {
                    // Tabs verticales para pantallas compactas
                    Column(
                            modifier =
                                    Modifier.fillMaxWidth()
                                            .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CompactFilterChip(
                                selected = showForegroundColorSelector,
                                onClick = onForegroundSelected,
                                label = stringResource(R.string.format_color),
                                iconRes = R.drawable.format_color_text,
                                iconSize = iconSize
                        )

                        CompactFilterChip(
                                selected = showBackgroundColorSelector,
                                onClick = onBackgroundSelected,
                                label = stringResource(R.string.format_bg_color),
                                iconRes = R.drawable.format_color_text,
                                iconSize = iconSize
                        )
                    }
                } else {
                    // Tabs horizontales para pantallas normales
                    Row(
                            modifier =
                                    Modifier.fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FilterChip(
                                selected = showForegroundColorSelector,
                                onClick = onForegroundSelected,
                                label = { Text(stringResource(R.string.format_color)) },
                                leadingIcon = {
                                    Icon(
                                            painter = painterResource(R.drawable.format_color_text),
                                            contentDescription = null,
                                            modifier = Modifier.size(iconSize)
                                    )
                                }
                        )

                        Spacer(modifier = Modifier.width(buttonSpacing))

                        FilterChip(
                                selected = showBackgroundColorSelector,
                                onClick = onBackgroundSelected,
                                label = { Text(stringResource(R.string.format_bg_color)) },
                                leadingIcon = {
                                    Icon(
                                            painter = painterResource(R.drawable.format_color_text),
                                            contentDescription = null,
                                            modifier = Modifier.size(iconSize)
                                    )
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(sectionSpacing))

                // Color selector - Grid adaptativo
                when {
                    showForegroundColorSelector -> {
                        DropdownSectionHeader(
                                text = stringResource(R.string.format_color),
                                isCompact = isCompact
                        )
                        IRCColorSelector(
                                selectedColor = defaultColor,
                                onColorSelected = { colorCode ->
                                    onApplyColor(colorCode)
                                    onDismiss()
                                },
                                isCompact = isCompact,
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .padding(horizontal = if (isCompact) 4.dp else 8.dp)
                        )
                    }
                    showBackgroundColorSelector -> {
                        DropdownSectionHeader(
                                text = stringResource(R.string.format_bg_color),
                                isCompact = isCompact
                        )
                        IRCColorSelector(
                                selectedColor = currentBgColor,
                                onColorSelected = { bgColorCode ->
                                    onApplyBackground(bgColorCode)
                                    onDismiss()
                                },
                                isCompact = isCompact,
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .padding(horizontal = if (isCompact) 4.dp else 8.dp)
                        )

                        TextButton(
                                onClick = {
                                    onClearBackground()
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
                                    stringResource(R.string.no_background_color),
                                    style =
                                            if (isCompact) MaterialTheme.typography.bodySmall
                                            else MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
