package hampusborg.bankapp.application.dto.response

data class TransactionDetailsResponse(
    val fromAccountId: String,
    val toAccountId: String,
    val amount: Double,
    val timestamp: Long,
    val categoryId: String
)