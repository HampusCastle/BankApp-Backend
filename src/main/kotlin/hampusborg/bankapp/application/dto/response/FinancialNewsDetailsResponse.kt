package hampusborg.bankapp.application.dto.response

data class FinancialNewsDetailsResponse(
    val title: String,
    val description: String?,
    val source: String,
    val url: String
)