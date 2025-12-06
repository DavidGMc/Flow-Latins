package cd.software.flowchat.presentation.chat.components.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Botón flotante para hacer scroll al final de la conversación Muestra un badge con el número de
 * mensajes no leídos si es mayor a 0
 */
@Composable
fun FloatingScrollButton(onClick: () -> Unit, modifier: Modifier = Modifier, unreadCount: Int = 0) {
    val scrollButtonColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
    val scrollButtonContentColor = MaterialTheme.colorScheme.onPrimary
    val badgeColor = MaterialTheme.colorScheme.error

    Box(modifier = modifier) {
        Card(
                modifier = Modifier.size(40.dp).clickable(onClick = onClick),
                shape = CircleShape,
                elevation = CardDefaults.cardElevation(4.dp),
                colors =
                        CardDefaults.cardColors(
                                containerColor = scrollButtonColor,
                                contentColor = scrollButtonContentColor
                        )
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                if (unreadCount > 0) {
                    BadgedBox(
                            badge = {
                                Badge(
                                        containerColor = badgeColor,
                                        contentColor = MaterialTheme.colorScheme.onError
                                ) {
                                    Text(
                                            text =
                                                    if (unreadCount > 99) "99+"
                                                    else unreadCount.toString(),
                                            style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                    ) {
                        Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = "Scroll to bottom",
                                modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Scroll to bottom",
                            modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
