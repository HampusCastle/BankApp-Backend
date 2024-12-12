package hampusborg.bankapp.application.dto.response

data class AccountResponse(
    val id: String,
    val name: String,
    val balance: Double,
    val accountType: String,
    val userId: String,
)