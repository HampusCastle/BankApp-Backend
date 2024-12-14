package hampusborg.bankapp.application.dto.response

data class NotificationDetailsResponse(
    val id: String?,
    val userId: String,
    val message: String,
    val timestamp: Long,
    val type: String
)