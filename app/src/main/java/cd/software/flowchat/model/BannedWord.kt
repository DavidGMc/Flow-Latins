package cd.software.flowchat.model

// data/domain/model/BannedWord.kt
data class BannedWord(
    val word: String,
    val severity: Severity = Severity.MEDIUM,
    val replacement: String? = null
) {
    enum class Severity { LOW, MEDIUM, HIGH }
}
