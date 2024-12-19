package hampusborg.bankapp.application.dto.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class InitiateTransferRequest(
    @field:NotEmpty(message = "Source account ID cannot be empty")
    val fromAccountId: String,

    @field:NotEmpty(message = "Destination account ID cannot be empty")
    val toAccountId: String,

    @field:Min(value = 1, message = "Amount must be greater than zero")
    val amount: Double,

    val categoryId: String? = null
)