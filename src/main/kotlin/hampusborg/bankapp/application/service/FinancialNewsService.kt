package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.FetchFinancialNewsRequest
import hampusborg.bankapp.application.dto.response.FinancialNewsDetailsResponse
import hampusborg.bankapp.application.dto.response.ExternalNewsApiResponse
import hampusborg.bankapp.application.exception.classes.ApiRequestException
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.application.service.base.RateLimiterService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class FinancialNewsService(
    @Value("\${newsapi.api.key}") private val apiKey: String,
    private val rateLimiterService: RateLimiterService,
    private val cacheHelperService: CacheHelperService,
    private val webClient: WebClient
) {

    private val apiUrl = "https://newsapi.org/v2/everything?q=finance&apiKey=$apiKey"

    fun getFinancialNews(request: FetchFinancialNewsRequest): List<FinancialNewsDetailsResponse> {
        val userId = "financialNewsUser" // You can customize how to identify the user (e.g., from the request)

        if (!rateLimiterService.isAllowed(userId)) {
            throw ApiRequestException("Too many requests, please try again later.")
        }

        val cachedNews = cacheHelperService.getFinancialNews()
        if (cachedNews.isNotEmpty()) {
            return cachedNews
        }

        val url = "$apiUrl&category=${request.category}&page=${request.page}&pageSize=${request.pageSize}"

        return try {
            val response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(ExternalNewsApiResponse::class.java)
                .block()

            val articles = response?.articles ?: emptyList()

            val financialNews = articles.map { article ->
                FinancialNewsDetailsResponse(
                    title = article.title ?: "No title",
                    description = article.description ?: "No description",
                    source = article.source?.name ?: "Unknown source",
                    url = article.url ?: "No URL"
                )
            }

            cacheHelperService.storeFinancialNews(financialNews)

            financialNews
        } catch (e: Exception) {
            throw ApiRequestException("Failed to fetch financial news: ${e.message}")
        }
    }
}