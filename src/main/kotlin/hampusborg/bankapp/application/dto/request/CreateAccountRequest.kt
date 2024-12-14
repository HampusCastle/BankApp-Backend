package hampusborg.bankapp.application.dto.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class CreateAccountRequest(
    @field:NotEmpty(message = "Account name cannot be empty")
    val name: String,

    @field:NotNull(message = "Balance must not be null")
    @field:Min(value = 0, message = "Balance must be at least 0")
    val balance: Double,

    @field:NotEmpty(message = "Account type cannot be empty")
    val accountType: String,

    val userId: String? = null
)