package hampusborg.bankapp.application.dto.response

data class WithdrawFundsResponse(
    val id: String,
    val name: String,
    val balance: Double,
    val message: String
)