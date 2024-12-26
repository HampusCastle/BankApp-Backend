package hampusborg.bankapp.application.service
import hampusborg.bankapp.application.dto.request.GetMarketTrendsRequest
import hampusborg.bankapp.application.dto.response.MarketTrendsDetailsResponse
import hampusborg.bankapp.application.exception.classes.ApiRequestException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class MarketTrendsService(
    @Value("\${financial.api.key}") private val financialApiKey: String,
    private val restTemplate: RestTemplate
) {

    private val log = LoggerFactory.getLogger(MarketTrendsService::class.java)

    fun getMarketTrends(request: GetMarketTrendsRequest): MarketTrendsDetailsResponse {
        val url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=${request.symbol}&apikey=$financialApiKey"
        return try {
            val response = fetchMarketData(url)
            val quote = extractQuote(response)
            val trend = calculateTrend(quote["change"]?.toDoubleOrNull() ?: 0.0)

            MarketTrendsDetailsResponse(
                trend = trend,
                price = quote["price"] ?: "N/A",
                changePercent = quote["changePercent"] ?: "N/A"
            )
        } catch (e: Exception) {
            log.error("Error fetching market trends: ${e.message}", e)
            throw ApiRequestException("Failed to fetch market trends: ${e.message}")
        }
    }

    private fun fetchMarketData(url: String): Map<String, Any> {
        val response = restTemplate.getForObject(url, Map::class.java) as? Map<String, Any>
            ?: throw ApiRequestException("Empty response from API")

        if (response.containsKey("Error Message") || response.containsKey("Information")) {
            throw ApiRequestException("Error from API: ${response["Error Message"] ?: response["Information"]}")
        }

        return response
    }

    private fun extractQuote(response: Map<String, Any>): Map<String, String> {
        val globalQuote = response["Global Quote"] as? Map<String, Any>
            ?: throw ApiRequestException("Missing 'Global Quote' data in API response.")
        return mapOf(
            "price" to (globalQuote["05. price"] as? String ?: "N/A"),
            "change" to (globalQuote["09. change"] as? String ?: "0.0"),
            "changePercent" to (globalQuote["10. change percent"] as? String ?: "N/A")
        )
    }

    private fun calculateTrend(change: Double): String {
        return if (change > 0) "The market is trending upward." else "The market is trending downward."
    }
}
