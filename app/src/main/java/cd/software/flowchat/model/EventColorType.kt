package cd.software.flowchat.model


import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class EventColorType(val getColor: @Composable () -> Color) {
    NICK_CHANGE({ MaterialTheme.colorScheme.primary }),
    USER_JOIN({ MaterialTheme.colorScheme.tertiary }),
    USER_PART({ MaterialTheme.colorScheme.error }),
    USER_QUIT({ MaterialTheme.colorScheme.errorContainer}),
    USER_KICK({ MaterialTheme.colorScheme.error }),
    USER_BAN({ MaterialTheme.colorScheme.onError }),
    USER_OP({ MaterialTheme.colorScheme.primary }),
    USER_DEOP({ MaterialTheme.colorScheme.secondary }),
    USER_VOICE({ MaterialTheme.colorScheme.tertiary }),
    SYSTEM_GENERAL({ MaterialTheme.colorScheme.outline }),
    SERVER_NOTICE({ MaterialTheme.colorScheme.inversePrimary}),
    TEXT({ MaterialTheme.colorScheme.onBackground }),
    USER_DEVOICE({ MaterialTheme.colorScheme.error }),
    USER_HALFOP({ MaterialTheme.colorScheme.primary }),
    USER_DEHALFOP({ MaterialTheme.colorScheme.secondary }),
    MODE_CHANGE({ MaterialTheme.colorScheme.primary }),
    USER_DISCONNECTED({ MaterialTheme.colorScheme.error }),
    NOTICE({MaterialTheme.colorScheme.inversePrimary}),
    TOPIC_CHANGE({MaterialTheme.colorScheme.onTertiary}),
    ERROR({MaterialTheme.colorScheme.onError}),
    SYSTEM_SYNC({ MaterialTheme.colorScheme.primary }),
    ACTION({ MaterialTheme.colorScheme.primary }),
    INVITE({ MaterialTheme.colorScheme.primary }),
    SYSTEM_INFO({ MaterialTheme.colorScheme.primary });


    // Función de utilidad para obtener el Color en un contexto @Composable
    @Composable
    fun toColor(): Color = getColor()
}