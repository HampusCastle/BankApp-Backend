package hampusborg.bankapp.application.dto.response

data class ExpensesSummaryResponse(
    val totalExpenses: Double,
    val categories: Map<String, Double> = emptyMap()
)