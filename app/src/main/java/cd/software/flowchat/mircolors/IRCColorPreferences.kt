package cd.software.flowchat.mircolors

import androidx.compose.ui.text.AnnotatedString
import cd.software.flowchat.model.IRCMessage
import cd.software.flowchat.preferences.ChatPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn

class IRCColorPreferences(
    private val chatPreferences: ChatPreferences
) {

    companion object {
        const val KEY_USE_COLORS = "use_irc_colors_latins"
        const val KEY_DEFAULT_COLOR = "default_irc_color_latins"
        private const val KEY_BACKGROUND_COLOR = "background_color_latins"
        private val USE_COLORS_BY_DEFAULT_KEY = "use_colors_by_default_latins"
        private val AUTO_APPLY_COLORS_KEY = "auto_apply_irc_colors_latins"
        private val KEY_DISABLE_COLORS = "disable_color_codes_latins"

    }
    // Usamos StateFlow para acceso inmediato a los valores
    val useColors: StateFlow<Boolean> =
        chatPreferences.getBooleanPreference(KEY_USE_COLORS, true)
            .stateIn(
                scope = CoroutineScope(Dispatchers.Default),
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = true
            )

    val defaultColor: StateFlow<Int> =
        chatPreferences.getIntPreference(KEY_DEFAULT_COLOR, 1)
            .stateIn(
                scope = CoroutineScope(Dispatchers.Default),
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 1
            )

    val backgroundColor: StateFlow<Int> =
        chatPreferences.getIntPreference(KEY_BACKGROUND_COLOR, 0)
            .stateIn(
                scope = CoroutineScope(Dispatchers.Default),
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0
            )

    /**
     * Obtiene o establece si se deben mostrar los colores en los mensajes
     */
    suspend fun setUseColors(useColors: Boolean) {
        chatPreferences.setBooleanPreference(KEY_USE_COLORS, useColors)
    }

    /**
     * Obtiene o establece el color por defecto para los mensajes del usuario
     */
    suspend fun setDefaultColor(colorCode: Int) {
        if (colorCode in 0..15) {
            chatPreferences.setIntPreference(KEY_DEFAULT_COLOR, colorCode)
        }
    }

    fun getUseColors(): Flow<Boolean> {
        return chatPreferences.getBooleanPreference(KEY_USE_COLORS, true)
    }
    fun getDefaultColor(): Flow<Int> {
        return chatPreferences.getIntPreference(KEY_DEFAULT_COLOR, 1) // Negro por defecto
    }
    /**
     * Obtiene el color de fondo para el campo de chat
     * Retorna 0 para usar el color por defecto del tema
     */
    fun getBackgroundColor(): Flow<Int> {
        return chatPreferences.getIntPreference(KEY_BACKGROUND_COLOR, 0)
    }

    /**
     * Establece el color de fondo para el campo de chat
     * @param colorCode el código de color IRC (0-15) o 0 para usar el color por defecto
     */
    suspend fun setBackgroundColor(colorCode: Int) {
        chatPreferences.setIntPreference(KEY_BACKGROUND_COLOR, colorCode)
    }
    fun getUseColorsByDefault(): Flow<Boolean> {
        return chatPreferences.getBooleanPreference(USE_COLORS_BY_DEFAULT_KEY, true)
    }

    /**
     * Establece la preferencia de aplicar colores por defecto a los mensajes nuevos
     */
    suspend fun setUseColorsByDefault(useByDefault: Boolean) {
        chatPreferences.setBooleanPreference(USE_COLORS_BY_DEFAULT_KEY, useByDefault)
    }

    fun getDisableColorCodes(): Flow<Boolean> {
        return chatPreferences.getBooleanPreference(KEY_DISABLE_COLORS, false)
    }

    suspend fun setDisableColorCodes(disable: Boolean) {
        chatPreferences.setBooleanPreference(KEY_DISABLE_COLORS, disable)
    }
}

/**
 * Extension para IRCMessage que facilita el formateo de mensajes
 */
fun IRCMessage.getFormattedContent(): AnnotatedString {
    return IRCMessageFormatter().formatMessage(this.content)
}