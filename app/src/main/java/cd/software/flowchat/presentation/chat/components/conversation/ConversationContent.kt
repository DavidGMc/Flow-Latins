package cd.software.flowchat.presentation.chat.components.conversation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import cd.software.flowchat.admob.AdViewModel
import cd.software.flowchat.firebase.viewmodel.AuthViewModel
import cd.software.flowchat.model.Conversation
import cd.software.flowchat.presentation.MessageItem
import cd.software.flowchat.presentation.chat.components.common.FloatingScrollButton
import cd.software.flowchat.viewmodel.IRCChatViewModel
import kotlinx.coroutines.launch

/**
 * Componente principal para mostrar el contenido de una conversación
 * Incluye lista de mensajes, auto-scroll y botón flotante para ir al final
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConversationContent(
    conversation: Conversation,
    onNickClick: (String) -> Unit = {},
    onOpenPrivateChat: (String) -> Unit = {},
    chatViewModel: IRCChatViewModel,
    authViewModel: AuthViewModel,
    adViewModel: AdViewModel,
    navController: NavController,
    pagerState: PagerState
) {

    val listState = rememberLazyListState()
    val context = LocalContext.current

    // OPTIMIZACIÓN: Estado derivado para mejor performance
    val showScrollToBottom by remember {
        derivedStateOf {
            val visibleItemsInfo = listState.layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) false
            else visibleItemsInfo.last().index < conversation.messages.lastIndex - 2
        }
    }

    // OPTIMIZACIÓN: Efecto más eficiente para auto-scroll
    LaunchedEffect(conversation.messages.size) {
        if (conversation.messages.isNotEmpty()) {
            val lastIndex = conversation.messages.lastIndex
            val currentIndex = listState.firstVisibleItemIndex
            val visibleItems = listState.layoutInfo.visibleItemsInfo.size

            // Solo hacer scroll si estamos cerca del final
            if (lastIndex - currentIndex <= visibleItems + 3) {
                // Usar animateScrollToItem para transición suave
                listState.animateScrollToItem(lastIndex)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize(),
            flingBehavior = rememberSnapFlingBehavior(listState)
        ) {
            items(
                items = conversation.messages,
                key = { it.hashCode() } // OPTIMIZACIÓN: Keys únicos para mejor performance
            ) { message ->
                MessageItem(
                    message = message,
                    isOwnMessage = message.isOwnMessage,
                    onNickClick = onNickClick,
                    onOpenPrivateChat = onOpenPrivateChat,
                    chatViewModel = chatViewModel,
                    authViewModel = authViewModel,
                    adViewModel = adViewModel,
                    context = context,
                    navController = navController,
                    pagerState = pagerState,
                    conversations = chatViewModel.conversations.collectAsState().value,
                    fontSize = chatViewModel.fontSize.collectAsState().value
                )
            }
        }

        val coroutineScope = rememberCoroutineScope()
        // Botón de scroll optimizado
        AnimatedVisibility(
            visible = showScrollToBottom,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            FloatingScrollButton(
                modifier = Modifier.padding(16.dp),
                onClick = {
                    coroutineScope.launch {
                        // Scroll suave al final
                        listState.animateScrollToItem(conversation.messages.lastIndex)
                    }
                }
            )
        }
    }
}
