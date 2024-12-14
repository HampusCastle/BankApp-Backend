package hampusborg.bankapp.application.dto.response

data class RecurringPaymentResponse(
    val id: String,
    val userId: String,
    val amount: Double,
    val fromAccountId: String,
    val toAccountId: String,
    val interval: String,
    val status: String,
    val categoryId: String,
    val nextPaymentDate: Long
)