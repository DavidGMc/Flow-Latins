# Funciones a Eliminar del ChatScreen.kt

## IMPORTANTE: Guarda el archivo ChatScreen.kt antes de hacer cambios

## Paso 1: Eliminar Utilidades (Final del archivo)

### Eliminar líneas 3032-3034 - formatTimestamp
```kotlin
fun formatTimestamp(timestamp: Long): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
}//hola
```

### Eliminar líneas 2918-2926 - extractDomain
```kotlin
// Extrae el dominio de una URL
fun extractDomain(url: String): String {
    return try {
        val uri = Uri.parse(url)
        uri.host?.removePrefix("www.") ?: url
    } catch (e: Exception) {
        url
    }
}
```

### Eliminar líneas 2911-2917 - copyToClipboard
```kotlin
// Función para copiar al portapapeles
fun Context.copyToClipboard(text: String, label: String = "Mensaje") {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(this, "Copiado al portapapeles", Toast.LENGTH_SHORT).show()
}
```

### Eliminar líneas 2905-2910 - extractUrls
```kotlin
fun String.extractUrls(): List<Pair<Int, Int>> {
    val urlPattern = Regex("""https?://[^\s]+""")
    return urlPattern.findAll(this).map { match ->
        Pair(match.range.first, match.range.last + 1)
    }.toList()
}
```

## Paso 2: Eliminar Componentes

### Eliminar líneas 2997-3030 - UserSuggestionItem
```kotlin
@Composable
private fun UserSuggestionItem(
    username: String,
    isExactMatch: Boolean,
    onClick: () -> Unit
) {
    // ... todo el contenido ...
}
```

### Eliminar líneas 2928-2996 - UrlPreviewCard
```kotlin
@Composable
fun UrlPreviewCard(
    url: String,
    context: Context,
    modifier: Modifier = Modifier
) {
    // ... todo el contenido ...
}
```

### Eliminar líneas 2862-2904 - FormattedClickableText
```kotlin
@Composable
fun FormattedClickableText(
    message: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    onClickUrl: (String) -> Unit
) {
    // ... todo el contenido ...
}
```

### Eliminar líneas 2792-2861 - FloatingScrollButton
```kotlin
@Composable
fun FloatingScrollButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    unreadCount: Int = 0
) {
    // ... todo el contenido ...
}
```

### Eliminar líneas 2705-2760 - UserSuggestionsDropdown
```kotlin
@Composable
private fun UserSuggestionsDropdown(
    users: List<String>,
    query: String,
    onUserSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // ... todo el contenido ...
}
```

### Eliminar líneas 2662-2704 - SortedUserSuggestionsList
```kotlin
@Composable
private fun SortedUserSuggestionsList(
    users: List<String>,
    query: String,
    onUserSelected: (String) -> Unit
) {
    // ... todo el contenido ...
}
```

### Eliminar líneas 2634-2661 - FormatButton
```kotlin
@Composable
private fun FormatButton(
    icon: ImageVector,
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    // ... todo el contenido ...
}
```

### Eliminar líneas 2614-2630 - DropdownSectionHeader
```kotlin
@Composable
private fun DropdownSectionHeader(
    text: String,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    // ... todo el contenido ...
}
```

### Eliminar líneas 2583-2612 - CompactFilterChip
```kotlin
@Composable
private fun CompactFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    iconRes: Int,
    iconSize: Dp,
    modifier: Modifier = Modifier
) {
    // ... todo el contenido ...
}
```

### Eliminar líneas 2550-2581 - CompactFormatButton
```kotlin
@Composable
private fun CompactFormatButton(
    icon: ImageVector,
    onClick: () -> Unit,
    text: String,
    iconSize: Dp,
    modifier: Modifier = Modifier
) {
    // ... todo el contenido ...
}
```

### Eliminar líneas 2290-2548 - FormatOptionsDropdown
```kotlin
@Composable
private fun FormatOptionsDropdown(
    showFormatOptions: Boolean,
    onDismiss: () -> Unit,
    defaultColor: Int,
    currentBgColor: Int,
    // ... muchos parámetros ...
) {
    // ... todo el contenido (259 líneas) ...
}
```

### Eliminar líneas 2239-2288 - InputActionButton
```kotlin
@Composable
private fun InputActionButton(
    icon: Any,
    contentDescription: String,
    isActive: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // ... todo el contenido ...
}
```

### Eliminar líneas 2086-2236 - SimpleFormatOptionsDropdown
```kotlin
@Composable
private fun SimpleFormatOptionsDropdown(
    showFormatOptions: Boolean,
    onDismiss: () -> Unit,
    onInsertFormatting: (Char) -> Unit,
    onInsertReset: () -> Unit,
    onResetAll: () -> Unit,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    // ... todo el contenido (151 líneas) ...
}
```

## Resumen

Total de líneas a eliminar: **~1,334 líneas**

Después de eliminar estas funciones, el ChatScreen.kt pasará de 3,034 líneas a aproximadamente 1,700 líneas.

## Verificación

Después de hacer los cambios:
1. Asegúrate de que todos los imports estén agregados
2. Compila el proyecto: `./gradlew assembleDebug`
3. Verifica que no haya errores de compilación
