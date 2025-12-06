package cd.software.flowchat.mircolors

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle

class IRCMessageFormatter(
    private val useColors: Boolean = true,
    private val isDarkTheme: Boolean = false
) {
    /**
     * Convierte un mensaje IRC en AnnotatedString respetando preferencias de usuario
     */
    fun formatMessage(message: String): AnnotatedString {
        return if (useColors) {
            formatWithColors(message)
        } else {
            buildAnnotatedString {
                append(stripFormattingCodes(message))
            }
        }
    }

    private fun formatWithColors(message: String): AnnotatedString {
        return buildAnnotatedString {
            var i = 0
            var currentForeground: Color? = null
            var currentBackground: Color? = null
            var isBold = false
            var isItalic = false
            var isUnderlined = false

            while (i < message.length) {
                when (message[i]) {
                    IRCColors.COLOR_CHAR -> {
                        i++
                        val colorCode = parseColorCode(message, i)
                        i += colorCode.digitCount

                        currentForeground = if (colorCode.code >= 0) {
                            IRCColors.getColorByCode(colorCode.code, isDarkTheme)
                            //IRCColors.getColorByCode(colorCode.code)
                        } else {
                            null
                        }

                        if (i < message.length && message[i] == ',') {
                            i++
                            val bgColorCode = parseColorCode(message, i)
                            i += bgColorCode.digitCount

                            currentBackground = if (bgColorCode.code >= 0) {
                                IRCColors.getColorByCode(bgColorCode.code, isDarkTheme)
                               // IRCColors.getColorByCode(bgColorCode.code)
                            } else {
                                null
                            }
                        }
                    }
                    IRCColors.BOLD_CHAR -> {
                        isBold = !isBold
                        i++
                    }
                    IRCColors.ITALIC_CHAR -> {
                        isItalic = !isItalic
                        i++
                    }
                    IRCColors.UNDERLINE_CHAR -> {
                        isUnderlined = !isUnderlined
                        i++
                    }
                    IRCColors.RESET_CHAR -> {
                        currentForeground = null
                        currentBackground = null
                        isBold = false
                        isItalic = false
                        isUnderlined = false
                        i++
                    }
                    else -> {
                        val nextControlChar = findNextControlChar(message, i)
                        withStyle(createSpanStyle(
                            foreground = currentForeground,
                            background = currentBackground,
                            isBold = isBold,
                            isItalic = isItalic,
                            isUnderlined = isUnderlined
                        )) {
                            append(message.substring(i, nextControlChar))
                        }
                        i = nextControlChar
                    }
                }
            }
        }
    }

    private fun createSpanStyle(
        foreground: Color?,
        background: Color?,
        isBold: Boolean,
        isItalic: Boolean,
        isUnderlined: Boolean
    ): SpanStyle {
        return SpanStyle(
            color = foreground ?: Color.Unspecified,
            background = background ?: Color.Unspecified,
            fontWeight = if (isBold) FontWeight.Bold else null,
            fontStyle = if (isItalic) FontStyle.Italic else null,
            textDecoration = if (isUnderlined) TextDecoration.Underline else null
        )
    }

    private fun stripFormattingCodes(message: String): String {
        return message.replace(Regex("[\u0002-\u000F\u001D\u001F]|\\d{1,2}(,\\d{1,2})?"), "")
    }

    private data class ColorCodeResult(val code: Int, val digitCount: Int)

    private fun parseColorCode(message: String, startIndex: Int): ColorCodeResult {
        if (startIndex >= message.length) return ColorCodeResult(-1, 0)

        var code = ""
        var i = 0
        while (i < 2 && startIndex + i < message.length && message[startIndex + i].isDigit()) {
            code += message[startIndex + i]
            i++
        }

        return if (code.isNotEmpty()) {
            ColorCodeResult(code.toInt().coerceIn(0..15), code.length)
        } else {
            ColorCodeResult(-1, 0)
        }
    }

    private fun findNextControlChar(message: String, startIndex: Int): Int {
        val controlChars = listOf(
            IRCColors.COLOR_CHAR,
            IRCColors.BOLD_CHAR,
            IRCColors.ITALIC_CHAR,
            IRCColors.UNDERLINE_CHAR,
            IRCColors.RESET_CHAR
        )
        return (startIndex until message.length).firstOrNull {
            message[it] in controlChars
        } ?: message.length
    }
    fun stripAllFormats(text: String): String {
        if (text.isEmpty()) return text

        var result = text

        // Eliminar códigos de color con sus parámetros (incluye colores con fondo)
        // Patrón mejorado para capturar todos los formatos posibles de color
        result = result.replace(Regex("${Regex.escape(IRCColors.COLOR_CHAR.toString())}\\d{1,2}(,\\d{1,2})?"), "")

        // Eliminar caracteres de formato específicos
        result = result.replace(IRCColors.BOLD_CHAR.toString(), "")
        result = result.replace(IRCColors.ITALIC_CHAR.toString(), "")
        result = result.replace(IRCColors.UNDERLINE_CHAR.toString(), "")
        result = result.replace(IRCColors.RESET_CHAR.toString(), "")

        // Eliminar cualquier otro código de control o formato que pueda estar presente
        // Esto captura cualquier carácter de control (0x01-0x1F)
        result = result.replace(Regex("[\u0001-\u001F]"), "")

        return result
    }
    /**
     * Verifica si un texto contiene algún código de formato IRC
     */
    fun hasFormatting(text: String): Boolean {
        if (text.isEmpty()) return false

        // Busca cualquier código de formato IRC
        return text.contains(IRCColors.COLOR_CHAR) ||
                text.contains(IRCColors.BOLD_CHAR) ||
                text.contains(IRCColors.ITALIC_CHAR) ||
                text.contains(IRCColors.UNDERLINE_CHAR) ||
                text.contains(IRCColors.RESET_CHAR) ||
                text.contains(Regex("[\u0001-\u001F]"))
    }

    /**
     * Método de utilidad para imprimir los valores hexadecimales de los caracteres
     * de un texto (útil para depuración)
     */
    fun printHexValues(text: String): String {
        return text.toCharArray().joinToString(" ") {
            "0x" + Integer.toHexString(it.code).padStart(2, '0')
        }
    }



}