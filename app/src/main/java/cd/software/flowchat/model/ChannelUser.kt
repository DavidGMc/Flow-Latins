package cd.software.flowchat.model

data class ChannelUser(
    val nickname: String,
    val isOp: Boolean = false,     // Operador (@)
    val isOwner: Boolean = false,  // Owner (~)
    val isVoice: Boolean = false,  // Voice (+)
    val isHalfOp: Boolean = false, // Half-Op (%)
    val isAdmin: Boolean = false   // Admin (&)
) {
    fun getPrefixString(): String {
        return buildString {
            if (isOwner) append("~")
            if (isAdmin) append("&")
            if (isOp) append("@")
            if (isHalfOp) append("%")
            if (isVoice) append("+")
        }
    }

    // Para ordenar usuarios, primero por rango y luego por nombre
    fun getRankPriority(): Int {
        return when {
            isOwner -> 5
            isAdmin -> 4
            isOp -> 3
            isHalfOp -> 2
            isVoice -> 1
            else -> 0
        }
    }
}