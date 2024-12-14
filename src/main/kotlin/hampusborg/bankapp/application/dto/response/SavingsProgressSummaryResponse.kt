package hampusborg.bankapp.application.dto.response

data class SavingsProgressSummaryResponse(
    val totalSaved: Double,
    val savingsGoal: Double,
    val progressPercentage: Double
)