package hampusborg.bankapp.application.dto.response

data class SubscriptionResponse(
    val id: String,
    val userId: String,
    val amount: Double,
    val serviceName: String,
    val interval: String,
    val status: String,
    val categoryId: String?,
    val nextPaymentDate: Long,

)