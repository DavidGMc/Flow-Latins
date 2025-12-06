package cd.software.flowchat.mircolors

class IRCMessageComposer {
    private val stringBuilder = StringBuilder()

    /**
     * Añade texto con un color específico
     */
    fun addColoredText(text: String, colorCode: Int): IRCMessageComposer {
        stringBuilder.append(IRCColors.COLOR_CHAR)
        stringBuilder.append(colorCode.toString().padStart(2, '0'))
        stringBuilder.append(text)
        stringBuilder.append(IRCColors.RESET_CHAR)
        return this
    }

    /**
     * Añade texto con un color de fondo y frente específicos
     */
    fun addColoredText(text: String, foregroundCode: Int, backgroundCode: Int): IRCMessageComposer {
        stringBuilder.append(IRCColors.COLOR_CHAR)
        stringBuilder.append(foregroundCode.toString().padStart(2, '0'))
        stringBuilder.append(',')
        stringBuilder.append(backgroundCode.toString().padStart(2, '0'))
        stringBuilder.append(text)
        stringBuilder.append(IRCColors.RESET_CHAR)
        return this
    }

    /**
     * Añade texto en negrita
     */
    fun addBoldText(text: String): IRCMessageComposer {
        stringBuilder.append(IRCColors.BOLD_CHAR)
        stringBuilder.append(text)
        stringBuilder.append(IRCColors.BOLD_CHAR)
        return this
    }

    /**
     * Añade texto en cursiva
     */
    fun addItalicText(text: String): IRCMessageComposer {
        stringBuilder.append(IRCColors.ITALIC_CHAR)
        stringBuilder.append(text)
        stringBuilder.append(IRCColors.ITALIC_CHAR)
        return this
    }

    /**
     * Añade texto subrayado
     */
    fun addUnderlinedText(text: String): IRCMessageComposer {
        stringBuilder.append(IRCColors.UNDERLINE_CHAR)
        stringBuilder.append(text)
        stringBuilder.append(IRCColors.UNDERLINE_CHAR)
        return this
    }

    /**
     * Añade texto normal sin formato
     */
    fun addText(text: String): IRCMessageComposer {
        stringBuilder.append(text)
        return this
    }

    /**
     * Devuelve el mensaje completo con los códigos de formato
     */
    fun build(): String {
        return stringBuilder.toString()
    }

    /**
     * Limpia el composer para un nuevo mensaje
     */
    fun clear() {
        stringBuilder.clear()
    }
}