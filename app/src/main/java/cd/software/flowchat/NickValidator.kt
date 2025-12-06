package cd.software.flowchat

import android.content.Context
import es.chat.R

object NickValidator {
    // Patrón para validar nicks de IRC
    private val VALID_NICK_PATTERN = Regex("^[A-Za-z_\\-\\[\\]\\\\^{}|`][A-Za-z0-9_\\-\\[\\]\\\\^{}|`]*$")

    /**
     * Valida si un nickname cumple con las reglas de IRC
     */
    fun isValidNick(nickname: String): Boolean {
        if (nickname.isEmpty() || nickname.length > 30) {
            return false
        }
        return VALID_NICK_PATTERN.matches(nickname)
    }

    /**
     * Limpia un nickname removiendo caracteres no permitidos
     */
    fun sanitizeNick(nickname: String): String? {
        if (nickname.trim().isEmpty()) {
            return null
        }

        val cleaned = nickname.replace(Regex("[^A-Za-z0-9_\\-\\[\\]\\\\^{}|`]"), "_")

        return if (cleaned[0].isDigit()) {
            "_$cleaned"
        } else {
            cleaned
        }.take(30)
    }

    /**
     * Obtiene la razón por la que un nickname es inválido
     */
    fun getInvalidReason(nickname: String, context: Context): String? {
        return when {
            nickname.isEmpty() -> context.getString(R.string.nick_error_empty)
            nickname.length > 30 -> context.getString(R.string.nick_error_too_long)
            nickname[0].isDigit() -> context.getString(R.string.nick_error_starts_with_number)
            !VALID_NICK_PATTERN.matches(nickname) -> context.getString(R.string.nick_error_invalid_chars)
            else -> null
        }
    }
}