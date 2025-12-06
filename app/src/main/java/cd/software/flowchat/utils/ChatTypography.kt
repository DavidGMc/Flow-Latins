package cd.software.flowchat.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

object ChatTypography {
    // Estándares tipográficos para la app
    @Composable
    private fun scalableFontSize(baseSize: Int, fontSize: Int): TextUnit {
        // Escala proporcionalmente basado en el tamaño seleccionado
        return (baseSize + (fontSize - 14)).sp
    }

   /* @Composable
    fun messageText(fontSize: Int, isOwn: Boolean = false): TextStyle {
        val baseSize = 14 // Tamaño base en sp
        return MaterialTheme.typography.bodyMedium.copy(
            fontSize = scalableFontSize(baseSize, fontSize),
            color = if (isOwn) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            lineHeight = scalableFontSize(baseSize + 4, fontSize)
        )
    }*/
    @Composable
    fun messageText(fontSize: Int, isOwn: Boolean = false): TextStyle {
        return MaterialTheme.typography.bodyMedium.copy(
            fontSize = fontSize.sp,
            color = if (isOwn) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface,
            lineHeight = (fontSize + 4).sp
        )
    }

    /*@Composable
    fun systemMessage(fontSize: Int): TextStyle {
        val baseSize = 12
        return MaterialTheme.typography.bodySmall.copy(
            fontSize = scalableFontSize(baseSize, fontSize),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }*/
    @Composable
    fun systemMessage(fontSize: Int): TextStyle {
        return MaterialTheme.typography.bodyMedium.copy(
            fontSize = fontSize.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
   /* @Composable
    fun nicknameText(fontSize: Int): TextStyle {
        val baseSize = 14
        return MaterialTheme.typography.bodyLarge.copy(
            fontSize = scalableFontSize(baseSize, fontSize),
            fontWeight = FontWeight.Bold
        )
    }*/
   @Composable
   fun nicknameText(fontSize: Int): TextStyle {
       return MaterialTheme.typography.bodyLarge.copy(
           fontSize = (fontSize + 2).sp,
           fontWeight = FontWeight.Bold
       )
   }

   /* @Composable
    fun timestampText(fontSize: Int): TextStyle {
        val baseSize = 10
        return MaterialTheme.typography.labelSmall.copy(
            fontSize = scalableFontSize(baseSize, fontSize),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }*/
   @Composable
   fun timestampText(fontSize: Int): TextStyle {
       return MaterialTheme.typography.labelSmall.copy(
           fontSize = (fontSize - 2).sp,
           color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
       )
   }
    @Composable
    fun actionButtonText(fontSize: Int): TextStyle {
        return MaterialTheme.typography.labelMedium.copy(
            fontSize = (fontSize - 1).sp
        )
    }


}