package hampusborg.bankapp.application.dto.request

data class RecurringPaymentRequest(
    val userId: String,
    val amount: Double,
    val fromAccountId: String,
    val toAccountId: String,
    val interval: String,
    val categoryId: String
)