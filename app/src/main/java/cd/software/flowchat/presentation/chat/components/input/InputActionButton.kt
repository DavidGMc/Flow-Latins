package cd.software.flowchat.presentation.chat.components.input

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Botón de acción para el input de chat
 * Soporta tanto ImageVector como Painter como icono
 */
@Composable
fun InputActionButton(
    icon: Any, // ImageVector o Painter
    contentDescription: String,
    isActive: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                if (isActive) colorScheme.primaryContainer else Color.Transparent,
                CircleShape
            )
    ) {
        when (icon) {
            is ImageVector -> {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = when {
                        !enabled -> colorScheme.onSurface.copy(alpha = 0.38f)
                        isActive -> colorScheme.onPrimaryContainer
                        else -> colorScheme.onSurface.copy(alpha = 0.6f)
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
            is Painter -> {
                Icon(
                    painter = icon,
                    contentDescription = contentDescription,
                    tint = when {
                        !enabled -> colorScheme.onSurface.copy(alpha = 0.38f)
                        isActive -> colorScheme.onPrimaryContainer
                        else -> colorScheme.onSurface.copy(alpha = 0.6f)
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
