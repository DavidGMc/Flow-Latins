package cd.software.flowchat.bannerwords



class ContentFilter {
    private val bannedWords = mapOf(
        "puta" to "p***",
        "mierda" to "m*****",
        "puto" to "p***",
        "verga" to "v****",
        "joder" to "j****"
    )

    fun filterText(text: String): String {
        var result = text
        bannedWords.forEach { (word, replacement) ->
            result = result.replace(word.toRegex(RegexOption.IGNORE_CASE), replacement)
        }
        return result
    }
}