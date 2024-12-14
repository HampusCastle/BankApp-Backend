package hampusborg.bankapp.application.dto.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

data class CreateSavingsGoalRequest(
    @field:NotEmpty(message = "Goal name cannot be empty")
    val name: String,

    @field:NotEmpty(message = "User ID cannot be empty")
    val userId: String,

    @field:NotNull(message = "Target amount cannot be null")
    @field:Min(value = 1, message = "Target amount must be greater than 0")
    val targetAmount: Double,

    @field:NotNull(message = "Target date cannot be null")
    val targetDate: LocalDate,

    @field:NotBlank val accountId: String
)