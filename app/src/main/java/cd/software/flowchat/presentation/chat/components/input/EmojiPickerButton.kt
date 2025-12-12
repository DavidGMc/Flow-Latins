package cd.software.flowchat.presentation.chat.components.input

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.emoji2.emojipicker.EmojiPickerView
import es.chat.R

/**
 * Botón que abre un selector de emojis en un ModalBottomSheet.
 * Usa la librería oficial AndroidX emoji2-emojipicker.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmojiPickerButton(
    onEmojiSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showEmojiPicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val context = LocalContext.current

    // Botón para abrir el emoji picker
    IconButton(
        onClick = { showEmojiPicker = true },
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_emoji),
            contentDescription = "Seleccionar emoji"
        )
    }

    // Modal Bottom Sheet con el emoji picker
    if (showEmojiPicker) {
        ModalBottomSheet(
            onDismissRequest = { showEmojiPicker = false },
            sheetState = sheetState
        ) {
            AndroidView(
                factory = { context ->
                    EmojiPickerView(context).apply {
                        setOnEmojiPickedListener { emoji ->
                            onEmojiSelected(emoji.emoji)
                            showEmojiPicker = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            )
        }
    }
}
