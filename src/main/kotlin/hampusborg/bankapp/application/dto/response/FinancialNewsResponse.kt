package hampusborg.bankapp.application.dto.response

data class FinancialNewsResponse(
    val title: String,
    val description: String?,
    val source: String,
    val url: String
)