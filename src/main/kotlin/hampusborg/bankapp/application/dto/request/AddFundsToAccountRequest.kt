package hampusborg.bankapp.application.dto.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class AddFundsToAccountRequest(
    @field:NotNull(message = "Amount cannot be null")
    @field:Min(value = 1, message = "Amount must be greater than zero")
    val amount: Double
)