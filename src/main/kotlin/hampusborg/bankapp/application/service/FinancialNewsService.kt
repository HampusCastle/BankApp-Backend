package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.exception.classes.ApiRequestException
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.application.service.base.ExternalApiNewsHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.slf4j.LoggerFactory

@Service
class FinancialNewsService(
    @Value("\${newsapi.api.key}") private val apiKey: String,
    private val cacheHelperService: CacheHelperService,
    private val restTemplate: RestTemplate
) {
    private val apiUrl = "https://newsapi.org/v2/top-headlines"
    private val logger = LoggerFactory.getLogger(FinancialNewsService::class.java)

    fun fetchFinancialNews(page: Int, pageSize: Int, category: String): List<ExternalApiNewsHandler.FinancialNewsDetailsResponse> {
        val cachedNews = cacheHelperService.getFinancialNews()
        if (cachedNews.isNotEmpty()) {
            logger.info("Returning cached financial news.")
            return cachedNews
        }

        val url = "$apiUrl?apiKey=$apiKey&category=$category&page=$page&pageSize=$pageSize"

        return try {
            logger.info("Fetching financial news from API: $url")
            val response = restTemplate.getForObject(url, ExternalApiNewsHandler.ExternalNewsApiResponse::class.java)

            if (response == null || response.articles.isEmpty()) {
                throw ApiRequestException("No articles found in the API response.")
            }

            val articles = response.articles
            val financialNews = articles.map { article ->
                ExternalApiNewsHandler.FinancialNewsDetailsResponse(
                    title = article.title,
                    description = article.description ?: "No description available",
                    source = article.source?.name ?: "Unknown source",
                    url = article.url
                )
            }

            cacheHelperService.storeFinancialNews(financialNews)

            financialNews
        } catch (e: Exception) {
            logger.error("Failed to fetch financial news: ${e.message}", e)
            throw ApiRequestException("Failed to fetch financial news: ${e.message}")
        }
    }
}