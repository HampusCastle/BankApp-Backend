package hampusborg.bankapp.application.dto.request

data class NotificationRequest(
    val userId: String,
    val message: String,
    val type: String
)