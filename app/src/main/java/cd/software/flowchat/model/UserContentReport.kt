package cd.software.flowchat.model

data class UserContentReport(
    val reportedUserId: String,
    val reportedContent: String,
    val reason: String,
    val timestamp: Long,
    val reportedBy: String,
    val reportTimestamp: Long,
    val status: ReportStatus = ReportStatus.PENDING
)
