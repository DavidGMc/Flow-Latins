package cd.software.flowchat.presentation.chat.utils

/**
 * Detecta si una URL es de YouTube
 * Soporta formatos comunes de YouTube:
 * - youtube.com/watch?v=
 * - youtu.be/
 * - youtube.com/embed/
 * - youtube.com/v/
 */
fun String.isYouTubeUrl(): Boolean {
    return this.contains("youtube.com/watch", ignoreCase = true) ||
            this.contains("youtu.be/", ignoreCase = true) ||
            this.contains("youtube.com/embed/", ignoreCase = true) ||
            this.contains("youtube.com/v/", ignoreCase = true)
}

/**
 * Extrae el ID del video de YouTube de una URL
 * Soporta formatos:
 * - https://www.youtube.com/watch?v=VIDEO_ID
 * - https://www.youtube.com/watch?v=VIDEO_ID&t=30s
 * - https://youtu.be/VIDEO_ID
 * - https://youtu.be/VIDEO_ID?t=30
 * - https://www.youtube.com/embed/VIDEO_ID
 * - https://www.youtube.com/v/VIDEO_ID
 * - https://m.youtube.com/watch?v=VIDEO_ID
 *
 * @return El ID del video (11 caracteres) o null si no se encuentra
 */
fun String.extractYouTubeVideoId(): String? {
    val patterns = listOf(
        // youtube.com/watch?v=VIDEO_ID (con o sin parámetros adicionales)
        Regex("""(?:youtube\.com|m\.youtube\.com)/watch\?v=([a-zA-Z0-9_-]{11})"""),
        // youtu.be/VIDEO_ID
        Regex("""youtu\.be/([a-zA-Z0-9_-]{11})"""),
        // youtube.com/embed/VIDEO_ID
        Regex("""youtube\.com/embed/([a-zA-Z0-9_-]{11})"""),
        // youtube.com/v/VIDEO_ID
        Regex("""youtube\.com/v/([a-zA-Z0-9_-]{11})"""),
        // youtube.com/shorts/VIDEO_ID
        Regex("""youtube\.com/shorts/([a-zA-Z0-9_-]{11})""")
    )

    patterns.forEach { pattern ->
        pattern.find(this)?.groupValues?.getOrNull(1)?.let { return it }
    }

    return null
}

/**
 * Extrae todas las URLs de YouTube de un texto con sus IDs
 * @return Lista de pares (URL completa, ID del video)
 */
fun String.extractYouTubeUrls(): List<Pair<String, String>> {
    val urlPattern = Regex("""https?://[^\s]+""")
    return urlPattern.findAll(this)
        .mapNotNull { match ->
            val url = match.value
            if (url.isYouTubeUrl()) {
                url.extractYouTubeVideoId()?.let { videoId ->
                    url to videoId
                }
            } else null
        }
        .toList()
}