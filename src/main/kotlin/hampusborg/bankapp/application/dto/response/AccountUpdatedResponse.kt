package hampusborg.bankapp.application.dto.response

data class AccountUpdatedResponse(
    val id: String,
    val name: String,
    val balance: Double,
    val message: String
)