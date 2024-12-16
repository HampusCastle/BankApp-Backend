package hampusborg.bankapp.application.service.base

class ExternalApiNewsHandler {

    data class Source(
        val id: String?,
        val name: String
    )

    data class FinancialArticleResponse(
        val title: String,
        val description: String?,
        val url: String,
        val source: Source?
    )

    data class FinancialNewsDetailsResponse(
        val title: String,
        val description: String?,
        val source: String,
        val url: String
    )

    data class ExternalNewsApiResponse(
        val articles: List<FinancialArticleResponse>
    )
}