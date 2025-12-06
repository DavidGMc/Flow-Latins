package cd.software.flowchat.presentation.chat.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Formatea un timestamp en formato HH:mm */
fun formatTimestamp(timestamp: Long): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
}
