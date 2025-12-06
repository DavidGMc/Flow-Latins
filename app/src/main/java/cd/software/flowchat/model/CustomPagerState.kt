package cd.software.flowchat.model

data class CustomPagerState(
    val currentPage: Int = 0,
    val pageCount: Int = 1,
    val isUserScrolling: Boolean = false,
    val pendingNavigation: String? = null
)
