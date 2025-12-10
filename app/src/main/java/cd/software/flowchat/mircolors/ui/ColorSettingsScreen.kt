import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import es.chat.R
import cd.software.flowchat.mircolors.IRCColorPreferences
import cd.software.flowchat.mircolors.IRCColors
import cd.software.flowchat.mircolors.IRCMessageComposer
import cd.software.flowchat.mircolors.IRCMessageFormatter
import cd.software.flowchat.mircolors.ui.IRCColorSelector
import cd.software.flowchat.viewmodel.IRCChatViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorSettingsScreen(
    viewModel: IRCChatViewModel,
    colorPreferences: IRCColorPreferences = remember { IRCColorPreferences(viewModel.chatPreferences) },
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val useColors by colorPreferences.getUseColors().collectAsState(initial = true)
    val defaultColor by colorPreferences.getDefaultColor().collectAsState(initial = 1)
    val backgroundColor by colorPreferences.getBackgroundColor().collectAsState(initial = 0)
    val disableColorCodes by colorPreferences.getDisableColorCodes().collectAsState(initial = false)

    // Estado para el ejemplo de mensaje
    val context = LocalContext.current

    val sampleMessage = remember {
        IRCMessageComposer()
            .addText(context.getString(R.string.sample_text_part1))
            .addBoldText(context.getString(R.string.sample_text_bold))
            .addText(context.getString(R.string.sample_text_part2))
            .addColoredText(context.getString(R.string.sample_text_colored), 4)
            .addText(context.getString(R.string.sample_text_part3))
            .addColoredText(context.getString(R.string.sample_text_colored_bg), 12, 8)
            .build()
    }


    // Ejemplo modificable para mostrar el reseteo de formato en vivo
    var modifiableSampleMessage by remember { mutableStateOf(sampleMessage) }

    val formatter = remember { IRCMessageFormatter() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.color_settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1) Opciones de colores
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.color_options_title),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(id = R.string.show_colors_in_messages))
                        Switch(
                            checked = useColors,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    colorPreferences.setUseColors(it)
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Vista previa de mensaje con colores
                    ColoredMessagePreview(
                        messageText = sampleMessage,
                        formatter = formatter
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(id = R.string.color_setting_description),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // 2) Modo texto plano
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        stringResource(id = R.string.plain_text_mode_title),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.remove_formatting_codes),
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                stringResource(R.string.remove_formatting_description),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        Switch(
                            checked = disableColorCodes,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    colorPreferences.setDisableColorCodes(it)
                                }
                            }
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Ejemplo de texto plano
                    if (disableColorCodes) {
                        val sampleColoredText = "\u0003041This is a text with \u0002format\u0002 and \u000312colors\u0003"
                        val formatter = remember { IRCMessageFormatter() }
                        val cleanedText = remember(sampleColoredText) { formatter.stripAllFormats(sampleColoredText) }

                        Column {
                            Text(
                                stringResource(R.string.example_text_with_format),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.surface,
                                shape = MaterialTheme.shapes.small,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = formatter.formatMessage(sampleColoredText),
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Divider(Modifier.padding(vertical = 8.dp))
                                    Text(
                                        text = stringResource(R.string.converted_to_plain_text, cleanedText),
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                }
                            }
                        }
                    }
                }
            }

            // 3) Color por defecto
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.default_color_title),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Texto informativo sobre cómo usar los colores
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_paleta_colors),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Los colores se aplicarán automáticamente cuando 'Mostrar colores' esté activado. También puedes usar el botón de paleta en el chat para activar/desactivar los colores.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(id = R.string.select_default_color),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    IRCColorSelector(
                        onColorSelected = { colorCode ->
                            coroutineScope.launch {
                                colorPreferences.setDefaultColor(colorCode)
                            }
                        },
                        selectedColor = defaultColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(id = R.string.selected_color, defaultColor),
                            style = MaterialTheme.typography.bodySmall
                        )

                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    colorPreferences.setDefaultColor(1) // 1 = negro en IRC
                                }
                            }
                        ) {
                            Text(stringResource(id = R.string.reset))
                        }
                    }
                }
            }

            // 4) Color de fondo
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.background_color_title),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(id = R.string.select_background_color),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    IRCColorSelector(
                        onColorSelected = { colorCode ->
                            coroutineScope.launch {
                                colorPreferences.setBackgroundColor(colorCode)
                            }
                        },
                        selectedColor = backgroundColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(
                            R.string.selected_background_color,
                            if (backgroundColor == 0) "Default" else backgroundColor.toString()
                        ))

                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    colorPreferences.setBackgroundColor(0)
                                }
                            }
                        ) {
                            Text(stringResource(id = R.string.reset))
                        }
                    }
                }
            }

            // 5) Vista previa combinada (color por defecto + fondo)
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.combined_preview_title),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(id = R.string.combined_preview_description),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Vista previa combinada
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (backgroundColor == 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                else IRCColors.getColorByCode(backgroundColor)
                            )
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.example_message),
                            color = if (defaultColor == 0) MaterialTheme.colorScheme.onSurface
                            else IRCColors.getColorByCode(defaultColor),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // 6) Reseteo de formato
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.format_reset_title),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(id = R.string.remove_all_formats),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Vista previa con mensaje
                    Column {
                        Text(
                            stringResource(R.string.formatted_text_example),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        ColoredMessagePreview(
                            messageText = sampleMessage,
                            formatter = formatter
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            stringResource(R.string.plain_text_result),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        ColoredMessagePreview(
                            messageText = modifiableSampleMessage,
                            formatter = formatter
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botones
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                modifiableSampleMessage = formatter.stripAllFormats(sampleMessage)
                            }
                        ) {
                            Text(stringResource(id = R.string.remove_formats))
                        }

                        OutlinedButton(
                            onClick = {
                                modifiableSampleMessage = sampleMessage
                            }
                        ) {
                            Text(stringResource(id = R.string.restore_example))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Texto plano resultante
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = stringResource(R.string.plain_text_output),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "\"${formatter.stripAllFormats(modifiableSampleMessage)}\"",
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // 7) Guía de formato
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.formatting_guide_title),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.formatting_codes_usage),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    FormattingGuide()
                }
            }
        }
    }
}

@Composable
fun ColoredMessagePreview(
    messageText: String,
    formatter: IRCMessageFormatter
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = formatter.formatMessage(messageText),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun FormattingGuide() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FormatCode("${IRCColors.COLOR_CHAR}XX", "Color de texto (00-15)")
        FormatCode("${IRCColors.COLOR_CHAR}XX,YY", "Color de texto y fondo")
        FormatCode("${IRCColors.BOLD_CHAR}texto${IRCColors.BOLD_CHAR}", "Texto en negrita")
        FormatCode("${IRCColors.ITALIC_CHAR}texto${IRCColors.ITALIC_CHAR}", "Texto en cursiva")
        FormatCode("${IRCColors.UNDERLINE_CHAR}texto${IRCColors.UNDERLINE_CHAR}", "Texto subrayado")
        FormatCode("${IRCColors.RESET_CHAR}", "Restablecer todo el formato")

        Spacer(modifier = Modifier.height(8.dp))

        // Referencia de colores
        Text(
            text = stringResource(id = R.string.available_color_codes),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(4.dp))

        IRCColors.colorMap.entries.chunked(4).forEach { chunk ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                chunk.forEach { entry ->
                    ColorReference(entry.key, entry.value)
                }

                repeat(4 - chunk.size) {
                    Box(modifier = Modifier.width(48.dp))
                }
            }
        }
    }
}

@Composable
fun FormatCode(
    code: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .padding(end = 8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = code,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun ColorReference(
    colorCode: Int,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color)
        )
        Text(
            text = colorCode.toString().padStart(2, '0'),
            style = MaterialTheme.typography.bodySmall,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
    }
}