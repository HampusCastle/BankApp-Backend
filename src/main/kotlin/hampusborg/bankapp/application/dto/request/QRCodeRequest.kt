package hampusborg.bankapp.application.dto.request

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class QRCodeRequest(
    @field:NotEmpty(message = "From User ID cannot be empty")
    val fromUserId: String,

    @field:NotEmpty(message = "To User ID cannot be empty")
    val toUserId: String,

    @field:NotNull(message = "Amount cannot be null")
    val amount: Double
)