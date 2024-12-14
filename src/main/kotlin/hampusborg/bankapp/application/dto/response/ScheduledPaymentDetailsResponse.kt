package hampusborg.bankapp.application.dto.response

data class ScheduledPaymentDetailsResponse(
    val message: String,
    val paymentId: String? = null,
    val amount: Double? = null
)