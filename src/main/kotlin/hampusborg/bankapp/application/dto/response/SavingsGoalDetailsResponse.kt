package hampusborg.bankapp.application.dto.response

import java.time.LocalDate

data class SavingsGoalDetailsResponse(
    val id: String,
    val name: String,
    val userId: String,
    val targetAmount: Double,
    val targetDate: LocalDate,
    val currentAmount: Double,
    val accountId: String
)