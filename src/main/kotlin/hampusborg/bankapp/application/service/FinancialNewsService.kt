package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.FetchFinancialNewsRequest
import hampusborg.bankapp.application.dto.response.FinancialNewsDetailsResponse
import hampusborg.bankapp.application.dto.response.ExternalNewsApiResponse
import hampusborg.bankapp.application.exception.classes.ApiRequestException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class FinancialNewsService(
    @Value("\${newsapi.api.key}") private val apiKey: String
) {

    private val apiUrl = "https://newsapi.org/v2/everything?q=finance&apiKey=$apiKey"

    fun getFinancialNews(request: FetchFinancialNewsRequest): List<FinancialNewsDetailsResponse> {
        val restTemplate = RestTemplate()
        val url = "$apiUrl&category=${request.category}&page=${request.page}&pageSize=${request.pageSize}"

        try {
            val response = restTemplate.getForObject(url, ExternalNewsApiResponse::class.java)
            val articles = response?.articles ?: return emptyList()

            return articles.map { article ->
                FinancialNewsDetailsResponse(
                    title = article.title ?: "No title",
                    description = article.description ?: "No description",
                    source = article.source?.name ?: "Unknown source",
                    url = article.url ?: "No URL"
                )
            }
        } catch (e: Exception) {
            throw ApiRequestException("Failed to fetch financial news: ${e.message}")
        }
    }
}