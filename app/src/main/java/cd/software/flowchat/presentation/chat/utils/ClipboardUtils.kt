package cd.software.flowchat.presentation.chat.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

/** Copia texto al portapapeles y muestra un mensaje de confirmación */
fun Context.copyToClipboard(text: String, label: String = "Mensaje") {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(this, "Copiado al portapapeles", Toast.LENGTH_SHORT).show()
}
