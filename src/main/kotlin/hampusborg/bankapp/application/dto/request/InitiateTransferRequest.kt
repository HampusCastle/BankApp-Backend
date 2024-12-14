package hampusborg.bankapp.application.dto.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class InitiateTransferRequest(
    @field:NotEmpty(message = "From account ID cannot be empty")
    val fromAccountId: String,

    @field:NotEmpty(message = "To account ID cannot be empty")
    val toAccountId: String,

    @field:NotNull(message = "Amount must not be null")
    @field:Min(value = 1, message = "Amount must be at least 1")
    val amount: Double,

    @field:NotEmpty(message = "Category ID cannot be empty")
    val categoryId: String
)