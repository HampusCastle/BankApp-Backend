package hampusborg.bankapp.application.dto.response

data class ArticleResponse(
    val title: String,
    val description: String?,
    val source: SourceResponse,
    val url: String
)