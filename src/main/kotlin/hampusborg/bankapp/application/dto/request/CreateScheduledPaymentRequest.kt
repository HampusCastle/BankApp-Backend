package hampusborg.bankapp.application.dto.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

data class CreateScheduledPaymentRequest(
    @field:NotEmpty(message = "From account ID cannot be empty")
    val fromAccountId: String,

    @field:NotEmpty(message = "To account ID cannot be empty")
    val toAccountId: String,

    @field:NotNull(message = "Amount must not be null")
    @field:Min(value = 1, message = "Amount must be at least 1")
    val amount: Double,

    @field:NotNull(message = "Next payment date must not be null")
    val nextPaymentDate: Long,

    @field:NotEmpty(message = "Schedule cannot be empty")
    @field:Pattern(regexp = "daily|weekly|monthly", message = "Schedule must be 'daily', 'weekly', or 'monthly'")
    val schedule: String
)