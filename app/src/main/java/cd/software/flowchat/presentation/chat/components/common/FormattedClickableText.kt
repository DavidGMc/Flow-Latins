package cd.software.flowchat.presentation.chat.components.common

import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import cd.software.flowchat.mircolors.IRCMessageFormatter
import cd.software.flowchat.presentation.chat.utils.extractUrls

/** Texto formateado con soporte para formato IRC y URLs clickeables */
@Composable
fun FormattedClickableText(
        message: String,
        style: TextStyle,
        modifier: Modifier = Modifier,
        onClickUrl: (String) -> Unit
) {
    val formatter = remember { IRCMessageFormatter() }
    val urlRanges = remember(message) { message.extractUrls() }

    // Primero aplicamos el formato IRC
    val formattedText = formatter.formatMessage(message)

    // Luego añadimos las anotaciones de URLs
    val annotatedText = buildAnnotatedString {
        append(formattedText)

        urlRanges.forEach { (start, end) ->
            val url = message.substring(start, end)
            addStyle(
                    style =
                            SpanStyle(
                                    color = Color.Blue,
                                    textDecoration = TextDecoration.Underline
                            ),
                    start = start,
                    end = end
            )
            addStringAnnotation("URL", url, start, end)
        }
    }

    ClickableText(
            text = annotatedText,
            style = style,
            modifier = modifier,
            onClick = { offset ->
                annotatedText.getStringAnnotations("URL", offset, offset).firstOrNull()?.let {
                        annotation ->
                    onClickUrl(annotation.item)
                }
            }
    )
}
