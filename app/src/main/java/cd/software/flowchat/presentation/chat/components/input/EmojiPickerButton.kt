package cd.software.flowchat.presentation.chat.components.input

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.emoji2.emojipicker.EmojiPickerView
import es.chat.R

/**
 * Botón que muestra/oculta un selector de emojis debajo del input.
 * Usa la librería oficial AndroidX emoji2-emojipicker.
 * Permite seleccionar múltiples emojis sin cerrar el picker.
 */
@Composable
fun EmojiPickerButton(
    onEmojiSelected: (String) -> Unit,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Botón para abrir/cerrar el emoji picker
    InputActionButton(
        icon = painterResource(R.drawable.ic_emoji),
        contentDescription = "Seleccionar emoji",
        isActive = isExpanded,
        onClick = onToggle,
        modifier = modifier
    )
}

/**
 * Panel de emoji picker que se muestra debajo del input.
 */
@Composable
fun EmojiPickerPanel(
    visible: Boolean,
    onEmojiSelected: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Card(
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                // Header con botón cerrar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                // Emoji Picker
                AndroidView(
                    factory = { context ->
                        EmojiPickerView(context)
                    },
                    update = { view ->
                        // Actualizar el listener cada vez que cambia onEmojiSelected
                        // Esto evita el problema de closure stale
                        view.setOnEmojiPickedListener { emoji ->
                            onEmojiSelected(emoji.emoji)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
            }
        }
    }
}
