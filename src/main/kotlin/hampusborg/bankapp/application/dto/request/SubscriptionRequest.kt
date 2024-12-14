package hampusborg.bankapp.application.dto.request

data class SubscriptionRequest(
    val userId: String,
    val amount: Double,
    val serviceName: String,
    val interval: String,
    val categoryId: String? = null,
    val fromAccountId: String? = null,
    val toAccountId: String? = null
)