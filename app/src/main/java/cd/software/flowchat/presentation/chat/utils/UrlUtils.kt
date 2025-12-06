package cd.software.flowchat.presentation.chat.utils

import android.net.Uri

/** Extrae las URLs de un texto y devuelve sus posiciones */
fun String.extractUrls(): List<Pair<Int, Int>> {
    val urlPattern = Regex("""https?://[^\s]+""")
    return urlPattern
            .findAll(this)
            .map { match -> Pair(match.range.first, match.range.last + 1) }
            .toList()
}

/** Extrae el dominio de una URL */
fun extractDomain(url: String): String {
    return try {
        val uri = Uri.parse(url)
        uri.host?.removePrefix("www.") ?: url
    } catch (e: Exception) {
        url
    }
}
