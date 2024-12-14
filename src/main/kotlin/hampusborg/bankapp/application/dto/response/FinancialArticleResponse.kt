package hampusborg.bankapp.application.dto.response

data class FinancialArticleResponse(
    val title: String,
    val description: String?,
    val source: NewsSourceResponse,
    val url: String
)