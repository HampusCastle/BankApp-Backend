package hampusborg.bankapp.application.dto.request

data class SubscriptionRequest(
    val amount: Double,
    val serviceName: String,
    val interval: String,
    val categoryId: String? = null,
    val fromAccountId: String? = null,
    val toAccountId: String? = null
)