package hampusborg.bankapp.application.dto.request

data class SendNotificationRequest(
    val userId: String,
    val message: String,
    val type: String
)