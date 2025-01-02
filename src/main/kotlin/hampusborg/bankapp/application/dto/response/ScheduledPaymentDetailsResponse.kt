package hampusborg.bankapp.application.dto.response

data class ScheduledPaymentDetailsResponse(
    val message: String,
    val paymentId: String? = null,
    val fromAccountId: String? = null,
    val toAccountId: String? = null,
    val amount: Double? = null,
    val nextPaymentDate: Long? = null,
    val schedule: String? = null
)