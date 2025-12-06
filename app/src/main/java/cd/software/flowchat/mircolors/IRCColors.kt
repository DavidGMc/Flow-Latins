package cd.software.flowchat.mircolors

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme

object IRCColors {
    // Códigos de control IRC
    const val COLOR_CHAR = '\u0003' // Código para iniciar un color
    const val BOLD_CHAR = '\u0002'  // Código para negrita
    const val ITALIC_CHAR = '\u001D' // Código para cursiva
    const val UNDERLINE_CHAR = '\u001F' // Código para subrayado
    const val RESET_CHAR = '\u000F'  // Código para resetear formatos

    // Mapa de colores mIRC estándar (0-15)
    val colorMap = mapOf(
        0 to Color.White,        // Blanco
        1 to Color.Black,        // Negro
        2 to Color(0xFF00007F),  // Azul (Navy)
        3 to Color(0xFF009300),  // Verde
        4 to Color.Red,          // Rojo
        5 to Color(0xFF7F0000),  // Marrón (Castaño)
        6 to Color(0xFF9C009C),  // Púrpura
        7 to Color(0xFFFC7F00),  // Naranja
        8 to Color.Yellow,       // Amarillo
        9 to Color(0xFF00FC00),  // Verde Lima
        10 to Color(0xFF009393), // Verde Azulado
        11 to Color(0xFF00FFFF), // Cian (Aguamarina)
        12 to Color(0xFF0000FC), // Azul Real
        13 to Color(0xFFFF00FF), // Magenta (Fucsia)
        14 to Color(0xFF7F7F7F), // Gris
        15 to Color(0xFFD2D2D2)  // Gris Claro
    )


    /*fun getColorByCode(code: Int): Color {
        return colorMap[code] ?: colorMap[1]!! // Negro por defecto
    }*/
   fun getColorByCode(code: Int, isDarkTheme: Boolean = false): Color {
        return when {
            code == 0 && isDarkTheme -> Color.DarkGray
            code == 1 && !isDarkTheme -> Color.LightGray
            else -> colorMap[code] ?: Color.Unspecified
        }
   }
}