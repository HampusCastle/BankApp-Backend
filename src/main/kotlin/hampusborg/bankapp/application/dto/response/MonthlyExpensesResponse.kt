package hampusborg.bankapp.application.dto.response

data class MonthlyExpensesResponse(
    val totalExpenses: Double,
    val categories: Map<String, Double> = emptyMap()
)