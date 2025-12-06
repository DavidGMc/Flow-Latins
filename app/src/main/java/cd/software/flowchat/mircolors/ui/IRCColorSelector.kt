package cd.software.flowchat.mircolors.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cd.software.flowchat.mircolors.IRCColors

/**
 * Selector de colores IRC para componer mensajes
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IRCColorSelector(
    onColorSelected: (Int) -> Unit,
    selectedColor: Int = -1,
    isCompact: Boolean = false, // Nuevo parámetro
    modifier: Modifier = Modifier
) {
    // Configuración responsive
    val buttonSize = if (isCompact) 28.dp else 32.dp
    val spacing = if (isCompact) 6.dp else 8.dp
    val horizontalPadding = if (isCompact) 4.dp else 8.dp
    val verticalPadding = if (isCompact) 2.dp else 4.dp

    if (isCompact) {
        // FlowRow para pantallas compactas - evita el problema de LazyVerticalGrid en DropdownMenu
        FlowRow(
            modifier = modifier
                .fillMaxWidth()
                .padding(
                    horizontal = horizontalPadding,
                    vertical = verticalPadding
                )
                .heightIn(max = 120.dp)
                .verticalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            // Mostrar todos los colores disponibles
            for (colorEntry in IRCColors.colorMap) {
                ColorButton(
                    color = colorEntry.value,
                    colorCode = colorEntry.key,
                    isSelected = colorEntry.key == selectedColor,
                    onClick = { onColorSelected(colorEntry.key) },
                    size = buttonSize,
                    isCompact = isCompact
                )
            }
        }
    } else {
        // Scroll horizontal para pantallas normales (comportamiento original)
        Row(
            modifier = modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(
                    horizontal = horizontalPadding,
                    vertical = verticalPadding
                ),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            // Mostrar todos los colores disponibles
            for (colorEntry in IRCColors.colorMap) {
                ColorButton(
                    color = colorEntry.value,
                    colorCode = colorEntry.key,
                    isSelected = colorEntry.key == selectedColor,
                    onClick = { onColorSelected(colorEntry.key) },
                    size = buttonSize,
                    isCompact = isCompact
                )
            }
        }
    }
}

/**
 * Botón de color individual - Ahora responsive
 */
@Composable
private fun ColorButton(
    color: Color,
    colorCode: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    size: Dp = 32.dp,
    isCompact: Boolean = false
) {
    // Configuración responsive del botón
    val borderWidth = if (isSelected) {
        if (isCompact) 1.5.dp else 2.dp
    } else {
        if (isCompact) 0.8.dp else 1.dp
    }

    val textStyle = if (isCompact) {
        MaterialTheme.typography.labelSmall
    } else {
        MaterialTheme.typography.bodySmall
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
            .border(
                width = borderWidth,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                shape = CircleShape
            )
            .clickable(
                onClick = onClick,
                indication = null, // Sin efecto ripple para evitar deprecation
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        // Mostrar el número del color cuando está seleccionado
        if (isSelected) {
            Text(
                text = colorCode.toString(),
                color = getContrastColor(color),
                style = textStyle,
                fontWeight = FontWeight.Bold
            )
        }

        // Indicador visual adicional para modo compacto
        if (isCompact && isSelected) {
            Box(
                modifier = Modifier
                    .size(size * 0.3f)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
            )
        }
    }
}

/**
 * Determina el color de texto más legible según el fondo
 */
private fun getContrastColor(backgroundColor: Color): Color {
    val luminance = (0.299 * backgroundColor.red + 0.587 * backgroundColor.green + 0.114 * backgroundColor.blue)
    return if (luminance > 0.5f) Color.Black else Color.White
}

// Los imports necesarios que debes agregar en tu archivo:
// import androidx.compose.foundation.layout.ExperimentalLayoutApi
// import androidx.compose.foundation.layout.FlowRow
@Composable
private fun <T> LazyGridScope.items(
    items: List<T>,
    key: ((item: T) -> Any)? = null,
    span: (LazyGridItemSpanScope.(item: T) -> GridItemSpan)? = null,
    contentType: (item: T) -> Any? = { null },
    itemContent: @Composable LazyGridItemScope.(item: T) -> Unit
) {
    items(
        count = items.size,
        key = if (key != null) { index: Int -> key(items[index]) } else null,
        span = if (span != null) { { span(items[it]) } } else null,
        contentType = { index: Int -> contentType(items[index]) }
    ) {
        itemContent(items[it])
    }
}