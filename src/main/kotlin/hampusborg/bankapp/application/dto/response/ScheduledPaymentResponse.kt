package hampusborg.bankapp.application.dto.response

data class ScheduledPaymentResponse(
    val message: String,
    val paymentId: String? = null,
    val amount: Double? = null
)