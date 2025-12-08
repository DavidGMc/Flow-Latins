package cd.software.flowchat.presentation.chat.components.input

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cd.software.flowchat.presentation.ChatInput
import cd.software.flowchat.viewmodel.IRCChatViewModel
import kotlinx.coroutines.delay

/**
 * Sección optimizada de entrada de chat
 * Wrapper del ChatInput con optimizaciones de rendimiento y gestión del teclado
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun OptimizedChatInputSection(
    messageInput: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    users: List<String>,
    isKeyboardVisible: Boolean,
    keyboardHeight: Dp,
    chatViewModel: IRCChatViewModel,
    keyboardController: SoftwareKeyboardController?,
    modifier: Modifier = Modifier
) {
    var isSending by remember { mutableStateOf(false) }
    val disableColorCodes by chatViewModel.getDisableColorCodes().collectAsState()

    LaunchedEffect(isSending) {
        if (isSending) {
            delay(100)
            keyboardController?.hide()
            isSending = false
        }
    }

    val optimizedSendMessage: (String) -> Unit = { message ->
        if (message.isNotBlank() && !isSending) {
            isSending = true
            onSendMessage(message)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            ChatInput(
                message = messageInput,
                onMessageChange = onMessageChange,
                onSendMessage = optimizedSendMessage,
                users = users,
                disableColorCodes = disableColorCodes,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
    }
}
