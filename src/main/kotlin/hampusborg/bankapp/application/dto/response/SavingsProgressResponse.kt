package hampusborg.bankapp.application.dto.response

data class SavingsProgressResponse(
    val totalSaved: Double,
    val savingsGoal: Double,
    val progressPercentage: Double
)