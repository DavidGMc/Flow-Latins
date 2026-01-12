package cd.software.flowchat.presentation.chat.utils

import android.net.Uri
import java.util.regex.Pattern

/**
 * Patrones mejorados para detectar URLs:
 * - URLs con protocolo (http/https/ftp)
 * - URLs sin protocolo (www.dominio.com)
 * - URLs con subdominios
 * - URLs con puertos
 * - URLs con paths complejos
 */
private val URL_PATTERNS = listOf(
    // Protocolo completo
    Regex("""(?i)\b(?:https?|ftp)://[^\s<>"{}|\\^`\[\]]+"""),

    // Sin protocolo pero con www
    Regex("""(?i)\bwww\.[^\s<>"{}|\\^`\[\]]+\.[a-z]{2,}[^\s]*"""),

    // Dominios comunes sin www (evita falsos positivos con palabras)
    Regex("""(?i)\b(?!xn--)(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\.)+[a-z]{2,}[^\s]*""")
)

/**
 * Extensiones de imagen soportadas, incluyendo mayúsculas/minúsculas
 * Añadidas extensiones menos comunes pero válidas
 */
private val IMAGE_EXTENSIONS = setOf(
    "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg",
    "tiff", "tif", "ico", "heic", "heif", "avif", "apng",
    "jfif", "pjpeg", "pjp", "svgz"
)

/**
 * Dominios conocidos de hosting de imágenes que pueden no tener extensión
 * o usar URLs sin extensión
 */
private val IMAGE_HOSTING_DOMAINS = setOf(
    "imgur.com", "i.imgur.com",
    "gyazo.com",
    "prntscr.com", "prnt.sc",
    "cloudinary.com",
    "imageshack.us",
    "flickr.com",
    "photobucket.com",
    "postimg.cc", "postimages.org",
    "ibb.co", "imagebb.com"
)

/**
 * Patrones para detectar URLs que probablemente sean imágenes
 * basados en parámetros comunes
 */
private val IMAGE_URL_PATTERNS = listOf(
    Regex("""(?i)[?&](?:image|img|picture|photo|url|src|file)=[^&]+"""),
    Regex("""(?i)/render/image/.+"""),
    Regex("""(?i)/(?:thumb|thumbnail|small|medium|large|original)/"""),
    Regex("""(?i)/gallery/.+"""),
    Regex("""(?i)\.(?:${IMAGE_EXTENSIONS.joinToString("|")})(?:[?#].*)?$""")
)

/**
 * Extrae TODAS las URLs posibles de un texto
 * Incluye URLs con y sin protocolo
 */
fun String.extractAllUrls(): List<Pair<String, IntRange>> {
    val results = mutableListOf<Pair<String, IntRange>>()

    URL_PATTERNS.forEach { pattern ->
        pattern.findAll(this).forEach { matchResult ->
            var url = matchResult.value
            val range = matchResult.range

            // Si la URL no tiene protocolo, añadir https:// por defecto
            if (!url.matches(Regex("""^(?i)(https?|ftp)://.*"""))) {
                url = "https://$url"
            }

            // Normalizar la URL (eliminar caracteres no deseados al final)
            val normalizedUrl = normalizeUrl(url)

            // Evitar duplicados (por si varios patrones capturan la misma URL)
            if (results.none { it.second == range }) {
                results.add(Pair(normalizedUrl, range))
            }
        }
    }

    // Ordenar por posición en el texto
    return results.sortedBy { it.second.first }
}

/**
 * Versión mejorada de extractUrls que mantiene compatibilidad
 * pero usa la nueva lógica
 */
fun String.extractUrls(): List<Pair<Int, Int>> {
    return extractAllUrls().map { (_, range) ->
        Pair(range.first, range.last + 1)
    }
}

/**
 * Normaliza una URL eliminando caracteres no deseados al final
 */
private fun normalizeUrl(url: String): String {
    var normalized = url.trim()

    // Eliminar puntuación común que podría no ser parte de la URL
    val trailingChars = setOf(',', '.', ';', ':', '!', '?', ')', ']', '}')
    while (normalized.isNotEmpty() &&
        trailingChars.contains(normalized.last()) &&
        !normalized.endsWith("...")) {
        normalized = normalized.dropLast(1)
    }

    return normalized
}

/**
 * Extrae el dominio de una URL de forma más robusta
 * Maneja casos especiales y URLs mal formadas
 */
fun extractDomain(url: String): String {
    return try {
        var processedUrl = url
        // Asegurar que tenga protocolo para el parsing
        if (!processedUrl.matches(Regex("""^(?i)(https?|ftp)://.*"""))) {
            processedUrl = "https://$processedUrl"
        }

        val uri = Uri.parse(processedUrl)
        val host = uri.host ?: return url

        // Remover www. y otros subdominios comunes
        var domain = host.removePrefix("www.")
        domain = domain.removePrefix("m.")  // versión móvil
        domain = domain.removePrefix("i.")  // subdominio de imágenes

        // Para dominios cortos, verificar si es un TLD válido
        val parts = domain.split('.')
        if (parts.size >= 2) {
            // Mantener solo el dominio principal y TLD
            domain = "${parts[parts.size - 2]}.${parts.last()}"
        }

        domain
    } catch (e: Exception) {
        // Fallback: intentar extraer dominio manualmente
        extractDomainFallback(url)
    }
}

/**
 * Fallback manual para extracción de dominio
 */
private fun extractDomainFallback(url: String): String {
    val cleanUrl = if (url.contains("://")) {
        url.substringAfter("://")
    } else {
        url
    }

    val domainPart = cleanUrl.split('/').firstOrNull() ?: return url
    return domainPart.removePrefix("www.").removePrefix("m.")
}

/**
 * Detecta si una URL apunta a una imagen de forma más completa
 * Considera: extensiones, dominios de hosting, patrones en la URL
 */
fun String.isImageUrl(): Boolean {
    if (this.isBlank()) return false

    val cleanUrl = this.trim()

    // 1. Verificar por extensión (método original mejorado)
    if (hasImageExtension(cleanUrl)) {
        return true
    }

    // 2. Verificar si es de un dominio de hosting de imágenes
    if (isFromImageHostingDomain(cleanUrl)) {
        return true
    }

    // 3. Verificar patrones en la URL que indiquen imagen
    if (matchesImageUrlPattern(cleanUrl)) {
        return true
    }

    // 4. Verificar por content-type en query params (algunos servicios)
    if (hasImageContentTypeHint(cleanUrl)) {
        return true
    }

    return false
}

/**
 * Verifica si la URL tiene extensión de imagen
 */
private fun hasImageExtension(url: String): Boolean {
    val urlWithoutParams = url.split('?', '#', '&').first().lowercase()

    // Verificar extensión directa
    IMAGE_EXTENSIONS.forEach { ext ->
        if (urlWithoutParams.endsWith(".$ext") ||
            urlWithoutParams.contains(".$ext/") ||
            urlWithoutParams.contains(".$ext?")) {
            return true
        }
    }

    // Verificar extensiones con números (ej: .jpg2, .png123)
    val extensionPattern = Regex("""\.(?:${IMAGE_EXTENSIONS.joinToString("|")})\d*$""")
    if (extensionPattern.containsMatchIn(urlWithoutParams)) {
        return true
    }

    return false
}

/**
 * Verifica si la URL es de un dominio conocido de hosting de imágenes
 */
private fun isFromImageHostingDomain(url: String): Boolean {
    val domain = extractDomain(url).lowercase()

    // Verificar dominio exacto
    if (IMAGE_HOSTING_DOMAINS.any { domain == it.lowercase() }) {
        return true
    }

    // Verificar subdominios (ej: anything.imgur.com)
    IMAGE_HOSTING_DOMAINS.forEach { hostingDomain ->
        if (domain.endsWith(".$hostingDomain")) {
            return true
        }
    }

    return false
}

/**
 * Verifica si la URL coincide con patrones comunes de URLs de imágenes
 */
private fun matchesImageUrlPattern(url: String): Boolean {
    val lowerUrl = url.lowercase()

    // Verificar patrones predefinidos
    IMAGE_URL_PATTERNS.forEach { pattern ->
        if (pattern.containsMatchIn(lowerUrl)) {
            return true
        }
    }

    // Patrones adicionales dinámicos
    val additionalPatterns = listOf(
        "/image/",
        "/img/",
        "/photo/",
        "/picture/",
        "/uploads/",
        "/media/",
        "/assets/images/",
        "/static/images/",
        "format=jpg",
        "type=image",
        "output=image"
    )

    additionalPatterns.forEach { pattern ->
        if (lowerUrl.contains(pattern)) {
            return true
        }
    }

    return false
}

/**
 * Verifica si la URL tiene pistas de content-type en parámetros
 */
private fun hasImageContentTypeHint(url: String): Boolean {
    val lowerUrl = url.lowercase()

    val contentTypePatterns = listOf(
        "content-type=image",
        "mime=image",
        "format=image",
        "image/jpeg",
        "image/png",
        "image/gif",
        "image/webp"
    )

    contentTypePatterns.forEach { pattern ->
        if (lowerUrl.contains(pattern)) {
            return true
        }
    }

    return false
}

/**
 * Extrae URLs de imágenes de un texto usando la detección mejorada
 */
fun String.extractImageUrls(): List<String> {
    return extractAllUrls()
        .map { it.first }
        .filter { it.isImageUrl() }
        .distinct()  // Evitar duplicados
}

/**
 * Extrae solo las URLs que NO son imágenes
 */
fun String.extractNonImageUrls(): List<String> {
    return extractAllUrls()
        .map { it.first }
        .filterNot { it.isImageUrl() }
        .distinct()
}

/**
 * Clasifica todas las URLs encontradas en un texto
 * Devuelve un mapa con URLs de imágenes y no imágenes
 */
fun String.classifyUrls(): Map<String, List<String>> {
    val allUrls = extractAllUrls().map { it.first }.distinct()

    val imageUrls = mutableListOf<String>()
    val otherUrls = mutableListOf<String>()

    allUrls.forEach { url ->
        if (url.isImageUrl()) {
            imageUrls.add(url)
        } else {
            otherUrls.add(url)
        }
    }

    return mapOf(
        "images" to imageUrls,
        "links" to otherUrls
    )
}

/**
 * Verifica si una URL es probablemente un enlace a un video
 * (útil para futuras extensiones)
 */
fun String.isVideoUrl(): Boolean {
    val videoExtensions = setOf("mp4", "webm", "avi", "mov", "mkv", "flv", "wmv", "m4v")
    val urlWithoutParams = this.split('?', '#').first().lowercase()

    return videoExtensions.any { urlWithoutParams.endsWith(".$it") }
}

/**
 * Extrae el nombre de archivo de una URL de imagen
 */
fun String.extractImageFilename(): String {
    return try {
        val path = Uri.parse(this).path
        path?.substringAfterLast('/') ?: "image"
    } catch (e: Exception) {
        "image"
    }
}

/**
 * Extrae y clasifica TODOS los enlaces del texto, evitando duplicados
 * y separando por tipo de contenido. Mantiene compatibilidad con YouTube existente.
 */
fun String.extractAndClassifyLinks(): Map<String, List<String>> {
    // Primero extraer todas las URLs usando la nueva lógica mejorada
    val allUrls = extractAllUrls().map { it.first }.distinct()

    // Usar TU función existente de YouTube
    val youtubeUrls = allUrls.filter { it.isYouTubeUrl() }.distinct()

    // Luego identificar imágenes (excluyendo las que ya son videos de YouTube)
    val remainingUrls = allUrls - youtubeUrls.toSet()
    val imageUrls = remainingUrls.filter { it.isImageUrl() }

    // Lo que queda son URLs regulares (no YouTube, no imágenes)
    val regularUrls = remainingUrls - imageUrls.toSet()

    return mapOf(
        "youtube" to youtubeUrls,
        "images" to imageUrls,
        "links" to regularUrls
    )
}